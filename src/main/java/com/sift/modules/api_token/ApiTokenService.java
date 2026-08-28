package com.sift.modules.api_token;


import com.sift.modules.user.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

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

        UserEntity user = (UserEntity) authentication.getPrincipal();

        // Generate random raw token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken =
                "sift_" + Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(randomBytes);

        // hash this before saving.
        // We'll use SHA-256 for API tokens.
        String tokenHash = hashToken(rawToken);

        ApiTokenEntity apiToken = new ApiTokenEntity();

        apiToken.setUser(user);
        apiToken.setType(request.type());
        apiToken.setName(request.name());
        apiToken.setTokenHash(tokenHash);

        apiTokenRepository.save(apiToken);

        return new CreateApiTokenResponseDTO(
                apiToken.getId(),
                rawToken,
                apiToken.getType(),
                apiToken.getName()
        );
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}