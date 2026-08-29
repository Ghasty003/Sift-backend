package com.sift.modules.bookmark;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @GetMapping
    public List<BookmarkResponseDTO> getBookmarks(
            Authentication authentication
    ) {
        return bookmarkService.getBookmarks(authentication);
    }

    @GetMapping("/collection/{collectionId}")
    public ResponseEntity<List<BookmarkResponseDTO>> getBookmarksInCollection(
            Authentication authentication,
            @PathVariable UUID collectionId
    ) {
        return ResponseEntity.ok(
                bookmarkService.getBookmarksInCollection(
                        authentication,
                        collectionId
                )
        );
    }

    @PatchMapping("/{bookmarkId}/favorite")
    public ResponseEntity<BookmarkResponseDTO> toggleFavorite(
            Authentication authentication,
            @PathVariable UUID bookmarkId
    ) {
        return ResponseEntity.ok(
                bookmarkService.toggleFavorite(
                        authentication,
                        bookmarkId
                )
        );
    }


    @PatchMapping("/{bookmarkId}/read")
    public ResponseEntity<BookmarkResponseDTO> toggleRead(
            Authentication authentication,
            @PathVariable UUID bookmarkId
    ) {
        return ResponseEntity.ok(
                bookmarkService.toggleRead(
                        authentication,
                        bookmarkId
                )
        );
    }


    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(
            Authentication authentication,
            @PathVariable UUID bookmarkId
    ) {
        bookmarkService.deleteBookmark(
                authentication,
                bookmarkId
        );

        return ResponseEntity.noContent().build();
    }


    @PostMapping
    public BookmarkResponseDTO createBookmark(
            Authentication authentication,
            @RequestBody CreateBookmarkRequestDTO request
    ) {
        return bookmarkService.createBookmark(
                authentication,
                request
        );
    }

    @PatchMapping("/{bookmarkId}/collection/{collectionId}")
    public ResponseEntity<Void> addBookmarkToCollection(
            Authentication authentication,
            @PathVariable UUID bookmarkId,
            @PathVariable UUID collectionId
    ) {
        bookmarkService.addBookmarkToCollection(
                authentication,
                bookmarkId,
                collectionId
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<BookmarkResponseDTO>> getInboxBookmarks(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                bookmarkService.getInboxBookmarks(authentication)
        );
    }


    @GetMapping("/favorites")
    public ResponseEntity<List<BookmarkResponseDTO>> getFavoriteBookmarks(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                bookmarkService.getFavoriteBookmarks(authentication)
        );
    }


    @GetMapping("/unread")
    public ResponseEntity<List<BookmarkResponseDTO>> getUnreadBookmarks(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                bookmarkService.getUnreadBookmarks(authentication)
        );
    }

}
