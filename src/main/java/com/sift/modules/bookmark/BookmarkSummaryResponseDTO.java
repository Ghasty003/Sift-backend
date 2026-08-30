package com.sift.modules.bookmark;

import java.util.List;

public record BookmarkSummaryResponseDTO(
        long inboxCount,
        long favoriteCount,
        List<BookmarkResponseDTO> recentBookmarks
) {
}