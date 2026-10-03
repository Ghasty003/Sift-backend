package com.sift.modules.bookmark;

import java.time.OffsetDateTime;
import java.util.List;

public record QuotedTweetRequestDTO(
        String tweetId,
        String url,
        String authorUsername,
        String authorName,
        String authorAvatarUrl,
        String text,
        OffsetDateTime createdAt,
        List<TweetMediaRequestDTO> media
) {
}
