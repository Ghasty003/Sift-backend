package com.sift.modules.bookmark;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TweetResponseDTO(
        UUID tweetId,
        String url,
        String authorUsername,
        String authorName,
        String authorAvatarUrl,
        String text,
        OffsetDateTime createdAt,
        List<TweetMediaResponseDTO> media,
        boolean isReply,
        String replyToUsername,
        String repostedByName,
        String repostedByUsername,
        TweetResponseDTO quotedTweet
) {
}
