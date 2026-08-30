package com.sift.modules.api_token;

import java.time.OffsetDateTime;

public record ApiTokenResponseDTO(
        String tokenId,
        String name,
        String type,
        OffsetDateTime createdAt,
        OffsetDateTime lastUsedAt,
        OffsetDateTime revokedAt
) {
}