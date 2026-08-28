package com.sift.modules.bookmark;

import java.time.OffsetDateTime;

public record CreateBookmarkRequestDTO(
        String url,
        String tweetId,
        String authorUsername,
        String authorName,
        String text,
        OffsetDateTime createdAt
) {
}