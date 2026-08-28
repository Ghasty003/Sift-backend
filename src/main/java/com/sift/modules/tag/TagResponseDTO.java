package com.sift.modules.tag;

import java.util.UUID;

public record TagResponseDTO(
        UUID id,
        String name
) {
}