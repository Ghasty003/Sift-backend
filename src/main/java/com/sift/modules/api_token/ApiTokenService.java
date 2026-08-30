package com.sift.modules.api_token;

import com.sift.modules.user.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

@Service
public class ApiTokenService {

    private final ApiTokenRepository apiTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public ApiTokenService(ApiTokenRepository apiTokenRepository) {
        this.apiTokenRepository = apiTokenRepository;
    }

    public CreateApiTokenResponseDTO createToken(
            Authentication authentication,
            CreateApiTokenRequestDTO request
    ) {

        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        // Generate token ID
        String tokenId = UUID.randomUUID()
                .toString()
                .replace("-", "");

        // Generate random secret
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String secret = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Complete token given to the client
        String rawToken =
                "sift_" + tokenId + "_" + secret;

        // Store only the hash of the secret
        String tokenHash = hashToken(secret);

        ApiTokenEntity apiToken = new ApiTokenEntity();

        apiToken.setUser(user);
        apiToken.setTokenId(tokenId);
        apiToken.setTokenHash(tokenHash);
        apiToken.setType(request.type());
        apiToken.setName(request.name());

        apiTokenRepository.save(apiToken);

        return new CreateApiTokenResponseDTO(
                apiToken.getId(),
                rawToken,
                apiToken.getType(),
                apiToken.getName()
        );
    }

    @Transactional
    public Optional<UserEntity> authenticateToken(String rawToken) {

        if (rawToken == null || !rawToken.startsWith("sift_")) {
            return Optional.empty();
        }

        String[] parts = rawToken.split("_", 3);

        if (parts.length != 3) {
            return Optional.empty();
        }

        String tokenId = parts[1];
        String secret = parts[2];

        Optional<ApiTokenEntity> tokenOptional =
                apiTokenRepository.findByTokenIdAndRevokedAtIsNull(tokenId);

        if (tokenOptional.isEmpty()) {
            return Optional.empty();
        }

        ApiTokenEntity apiToken = tokenOptional.get();

        String providedHash = hashToken(secret);

        boolean valid = MessageDigest.isEqual(
                providedHash.getBytes(StandardCharsets.UTF_8),
                apiToken.getTokenHash()
                        .getBytes(StandardCharsets.UTF_8)
        );

        if (!valid) {
            return Optional.empty();
        }

        apiToken.setLastUsedAt(OffsetDateTime.now());

        return Optional.of(apiToken.getUser());
    }

    @Transactional
    public void revokeToken(
            Authentication authentication,
            String tokenId
    ) {
        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        ApiTokenEntity token =
                apiTokenRepository
                        .findByTokenIdAndUser(tokenId, user)
                        .orElseThrow(() ->
                                new RuntimeException("API token not found")
                        );

        if (token.getRevokedAt() != null) {
            return;
        }

        token.setRevokedAt(OffsetDateTime.now());

        apiTokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public List<ApiTokenResponseDTO> getUserTokens(
            Authentication authentication
    ) {
        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        return apiTokenRepository
                .findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(token -> new ApiTokenResponseDTO(
                        token.getTokenId(),
                        token.getName(),
                        token.getType(),
                        token.getCreatedAt(),
                        token.getLastUsedAt(),
                        token.getRevokedAt()
                ))
                .toList();
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to hash API token",
                    e
            );
        }
    }
}