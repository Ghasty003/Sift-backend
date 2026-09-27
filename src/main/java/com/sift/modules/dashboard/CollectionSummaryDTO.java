package com.sift.modules.dashboard;

import java.util.UUID;

public record CollectionSummaryDTO(
        UUID id,
        String name,
        long bookmarkCount,
        long unreadCount
) {
}