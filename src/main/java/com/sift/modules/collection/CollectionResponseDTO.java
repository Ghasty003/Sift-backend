package com.sift.modules.collection;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CollectionResponseDTO(
        UUID id,
        String name,
        String description,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}