package com.sift.modules.password_reset;

import com.sift.common.email.EmailService;
import com.sift.exceptions.InvalidResetTokenException;
import com.sift.modules.refresh_token.RefreshTokenService;
import com.sift.modules.user.UserEntity;
import com.sift.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private static final Duration TOKEN_VALIDITY = Duration.ofMinutes(30);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Always succeeds from the caller's perspective, whether or not the
     * email belongs to an account — otherwise this endpoint could be used
     * to check which emails have a Sift account.
     */
    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String rawToken = generateRawToken();

            PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
            entity.setUserId(user.getId());
            entity.setTokenHash(hash(rawToken));
            entity.setExpiresAt(OffsetDateTime.now().plus(TOKEN_VALIDITY));
            tokenRepository.save(entity);

            String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetTokenEntity entity = tokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidResetTokenException("Invalid or expired reset link"));

        if (entity.getUsedAt() != null) {
            throw new InvalidResetTokenException("This reset link has already been used");
        }
        if (entity.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new InvalidResetTokenException("This reset link has expired");
        }

        UserEntity user = userRepository.findById(entity.getUserId())
                .orElseThrow(() -> new InvalidResetTokenException("Invalid or expired reset link"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        entity.setUsedAt(OffsetDateTime.now());
        tokenRepository.save(entity);

        refreshTokenService.revokeAllSessionsForUser(user.getId());
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
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash reset token", e);
        }
    }
}