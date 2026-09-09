package com.sift.modules.user;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String email,
        String fullName,
        OffsetDateTime createdAt
) {
}