package com.sift.modules.bookmark;

import com.sift.common.CursorPageResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @GetMapping
    public CursorPageResponseDTO<BookmarkResponseDTO> getBookmarks(
            Authentication authentication,
            @RequestParam(required = false) String collectionId,
            @RequestParam(required = false) UUID tagId,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false, defaultValue = "false") boolean favoriteOnly,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort
    ) {
        return bookmarkService.searchBookmarks(
                authentication,
                new BookmarkFilterRequest(
                        collectionId, tagId, read, favoriteOnly, search, cursor, limit, sort
                )
        );
    }

    @PatchMapping("/{bookmarkId}/favorite")
    public ResponseEntity<BookmarkResponseDTO> toggleFavorite(
            Authentication authentication,
            @PathVariable UUID bookmarkId
    ) {
        return ResponseEntity.ok(bookmarkService.toggleFavorite(authentication, bookmarkId));
    }

    @PatchMapping("/{bookmarkId}/read")
    public ResponseEntity<BookmarkResponseDTO> toggleRead(
            Authentication authentication,
            @PathVariable UUID bookmarkId
    ) {
        return ResponseEntity.ok(bookmarkService.toggleRead(authentication, bookmarkId));
    }

    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(
            Authentication authentication,
            @PathVariable UUID bookmarkId
    ) {
        bookmarkService.deleteBookmark(authentication, bookmarkId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public BookmarkResponseDTO createBookmark(
            Authentication authentication,
            @RequestBody CreateBookmarkRequestDTO request
    ) {
        return bookmarkService.createBookmark(authentication, request);
    }

    @PatchMapping("/{bookmarkId}/collection/{collectionId}")
    public ResponseEntity<Void> addBookmarkToCollection(
            Authentication authentication,
            @PathVariable UUID bookmarkId,
            @PathVariable String collectionId
    ) {
        bookmarkService.addBookmarkToCollection(authentication, bookmarkId, collectionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<BookmarkSummaryResponseDTO> getBookmarkSummary(
            Authentication authentication
    ) {
        return ResponseEntity.ok(bookmarkService.getBookmarkSummary(authentication));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Map<String, Long>> markAllAsRead(Authentication authentication) {
        long updatedCount = bookmarkService.markAllAsRead(authentication);
        return ResponseEntity.ok(Map.of("updatedCount", updatedCount));
    }

    public record BulkIdsRequest(List<UUID> bookmarkIds) {}
    public record BulkMoveRequest(List<UUID> bookmarkIds, UUID collectionId) {}

    @PostMapping("/bulk-delete")
    public ResponseEntity<Map<String, Integer>> bulkDelete(
            Authentication authentication,
            @RequestBody BulkIdsRequest request
    ) {
        int count = bookmarkService.bulkDelete(authentication, request.bookmarkIds());
        return ResponseEntity.ok(Map.of("deletedCount", count));
    }

    @PostMapping("/bulk-move")
    public ResponseEntity<Map<String, Integer>> bulkMove(
            Authentication authentication,
            @RequestBody BulkMoveRequest request
    ) {
        int count = bookmarkService.bulkMove(authentication, request.bookmarkIds(), request.collectionId());
        return ResponseEntity.ok(Map.of("movedCount", count));
    }
}
