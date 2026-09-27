package com.sift.modules.dashboard;

import com.sift.modules.bookmark.BookmarkResponseDTO;

import java.util.List;

public record DashboardSummaryResponseDTO(
        long totalBookmarks,
        long inboxCount,
        long favoriteCount,
        long unreadCount,
        long collectionCount,
        long tagCount,
        List<BookmarkResponseDTO> recentBookmarks,
        List<CollectionSummaryDTO> recentCollections,
        List<TagSummaryDTO> popularTags
) {
}