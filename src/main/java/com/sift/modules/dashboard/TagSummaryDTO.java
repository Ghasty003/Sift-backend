package com.sift.modules.dashboard;

import java.util.UUID;

public record TagSummaryDTO(
        UUID id,
        String name,
        long bookmarkCount
) {
}