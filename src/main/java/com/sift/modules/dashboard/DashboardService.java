package com.sift.modules.dashboard;

import com.sift.modules.bookmark.BookmarkService;
import com.sift.modules.collection.CollectionResponseDTO;
import com.sift.modules.collection.CollectionService;
import com.sift.modules.tag.TagResponseDTO;
import com.sift.modules.tag.TagService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    private static final int RECENT_BOOKMARKS_LIMIT = 6;
    private static final int RECENT_COLLECTIONS_LIMIT = 5;
    private static final int POPULAR_TAGS_LIMIT = 10;

    private final BookmarkService bookmarkService;
    private final CollectionService collectionService;
    private final TagService tagService;

    public DashboardService(
            BookmarkService bookmarkService,
            CollectionService collectionService,
            TagService tagService
    ) {
        this.bookmarkService = bookmarkService;
        this.collectionService = collectionService;
        this.tagService = tagService;
    }

    public DashboardSummaryResponseDTO getSummary(Authentication authentication) {

        long totalBookmarks = bookmarkService.countAll(authentication);
        long inboxCount = bookmarkService.getBookmarkSummary(authentication).inboxCount();
        long favoriteCount = bookmarkService.getBookmarkSummary(authentication).favoriteCount();
        long unreadCount = bookmarkService.countUnread(authentication);

        List<CollectionResponseDTO> collections = collectionService.getCollections(authentication);
        List<TagResponseDTO> tags = tagService.getUserTags(authentication);

        List<CollectionSummaryDTO> recentCollections = collections.stream()
                .limit(RECENT_COLLECTIONS_LIMIT)
                .map(c -> new CollectionSummaryDTO(c.id(), c.name(), c.bookmarkCount(), c.unreadCount()))
                .toList();

        List<TagSummaryDTO> popularTags = tags.stream()
                .sorted(Comparator.comparingLong(TagResponseDTO::bookmarkCount).reversed())
                .limit(POPULAR_TAGS_LIMIT)
                .map(t -> new TagSummaryDTO(t.id(), t.name(), t.bookmarkCount()))
                .toList();

        return new DashboardSummaryResponseDTO(
                totalBookmarks,
                inboxCount,
                favoriteCount,
                unreadCount,
                collections.size(),
                tags.size(),
                bookmarkService.getRecentBookmarks(authentication, RECENT_BOOKMARKS_LIMIT),
                recentCollections,
                popularTags
        );
    }
}