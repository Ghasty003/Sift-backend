package com.sift.modules.bookmark;

import java.time.OffsetDateTime;
import java.util.List;

public record CreateBookmarkRequestDTO(
        String url,
        String tweetId,
        String authorUsername,
        String authorName,
        String authorAvatarUrl,
        String text,
        OffsetDateTime createdAt,
        List<TweetMediaRequestDTO> media,
        boolean isReply,
        String replyToUsername,
        String repostedByName,
        String repostedByUsername,
        QuotedTweetRequestDTO quotedTweet
) {
}
