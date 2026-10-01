package com.sift.modules.bookmark;

import java.time.OffsetDateTime;

public record CreateBookmarkRequestDTO(
        String url,
        String tweetId,
        String authorUsername,
        String authorName,
        String authorAvatarUrl,
        String text,
        OffsetDateTime createdAt,
        boolean isReply,
        String replyToUsername,
        String repostedByName,
        String repostedByUsername,
        QuotedTweetRequestDTO quotedTweet
) {
}