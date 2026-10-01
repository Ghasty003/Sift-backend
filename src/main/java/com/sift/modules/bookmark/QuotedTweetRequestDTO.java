package com.sift.modules.bookmark;

import java.time.OffsetDateTime;

public record QuotedTweetRequestDTO(
        String tweetId,
        String url,
        String authorUsername,
        String authorName,
        String authorAvatarUrl,
        String text,
        OffsetDateTime createdAt
) {
}