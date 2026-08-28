package com.sift.modules.note;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NoteResponseDTO(
        UUID id,
        UUID bookmarkId,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}