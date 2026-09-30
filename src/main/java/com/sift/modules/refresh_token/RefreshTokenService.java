package com.sift.modules.refresh_token;

import com.sift.exceptions.InvalidRefreshTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public record IssuedToken(String rawToken, UUID userId, UUID familyId) {}

    /** Starts a brand-new session lineage — called on login/register (one per device). */
    @Transactional
    public IssuedToken issue(UUID userId) {
        UUID familyId = UUID.randomUUID();
        String rawToken = createAndPersist(userId, familyId);
        return new IssuedToken(rawToken, userId, familyId);
    }

    /**
     * Validates + rotates a refresh token (sliding expiration: every
     * successful rotation resets the family's window to a full
     * refreshExpirationMs from now, which is what keeps an active user
     * logged in indefinitely without ever needing to re-enter credentials).
     *
     * - Unknown or expired token -> InvalidRefreshTokenException.
     * - A token that's already been rotated before (revokedAt already set)
     *   being presented again means it was copied/stolen and replayed after
     *   the legitimate client already moved on -> the WHOLE family is
     *   revoked, forcing a fresh login on every device using that session.
     * - Otherwise: the current token is marked consumed and a new one is
     *   issued in the same family.
     */
    @Transactional
    public IssuedToken rotate(String rawToken) {
        String hash = hash(rawToken);

        RefreshTokenEntity current = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (current.getRevokedAt() != null) {
            revokeFamily(current.getFamilyId());
            throw new InvalidRefreshTokenException("Refresh token reuse detected — session revoked");
        }

        if (current.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        String newRawToken = createAndPersist(current.getUserId(), current.getFamilyId());

        RefreshTokenEntity replacement = refreshTokenRepository
                .findByTokenHash(hash(newRawToken))
                .orElseThrow();

        current.setRevokedAt(OffsetDateTime.now());
        current.setReplacedById(replacement.getId());
        refreshTokenRepository.save(current);

        return new IssuedToken(newRawToken, current.getUserId(), current.getFamilyId());
    }

    /** Revokes only the session (family/device) the given token belongs to — used for logout. */
    @Transactional
    public void revokeByToken(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .ifPresent(token -> revokeFamily(token.getFamilyId()));
    }

    private void revokeFamily(UUID familyId) {
        List<RefreshTokenEntity> active =
                refreshTokenRepository.findByFamilyIdAndRevokedAtIsNull(familyId);

        OffsetDateTime now = OffsetDateTime.now();
        active.forEach(t -> t.setRevokedAt(now));
        refreshTokenRepository.saveAll(active);
    }

    private String createAndPersist(UUID userId, UUID familyId) {
        String rawToken = generateRawToken();

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUserId(userId);
        entity.setFamilyId(familyId);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(OffsetDateTime.now().plus(Duration.ofMillis(refreshExpirationMs)));

        refreshTokenRepository.save(entity);
        return rawToken;
    }

    /**
     * Signs the user out of every active session/device — used after a
     * password reset, since that's a strong "treat this account as
     * potentially compromised" signal.
     */
    @Transactional
    public void revokeAllSessionsForUser(UUID userId) {
        List<RefreshTokenEntity> active = refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId);
        OffsetDateTime now = OffsetDateTime.now();
        active.forEach(t -> t.setRevokedAt(now));
        refreshTokenRepository.saveAll(active);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}