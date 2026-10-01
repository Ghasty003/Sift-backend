package com.sift.modules.bookmark;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TweetResponseDTO(
        UUID tweetId,
        String url,
        String authorUsername,
        String authorName,
        String authorAvatarUrl,
        String text,
        OffsetDateTime createdAt,
        boolean isReply,
        String replyToUsername,
        String repostedByName,
        String repostedByUsername,
        TweetResponseDTO quotedTweet
) {
}