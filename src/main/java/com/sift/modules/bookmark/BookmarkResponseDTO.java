package com.sift.modules.bookmark;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookmarkResponseDTO(UUID id,
                                  UUID tweetId,
                                  String url,
                                  String authorUsername,
                                  String authorName,
                                  String text,
                                  OffsetDateTime createdAt,
                                  boolean favorite, boolean read,
                                  OffsetDateTime savedAt) {
}
