package com.sift.modules.bookmark;

import java.util.UUID;

public record BookmarkFilterRequest(
        // "inbox" for uncategorized, a collection UUID string, or null for no filter
        String collectionId,
        UUID tagId,
        Boolean read,
        boolean favoriteOnly,
        String search,
        String cursor,
        Integer limit
) {
}