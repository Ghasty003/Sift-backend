package com.sift.modules.bookmark;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TweetResponseDTO(
        UUID tweetId,
        String url,
        String authorUsername,
        String authorName,
        String text,
        OffsetDateTime createdAt
) {
}
