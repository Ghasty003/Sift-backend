package com.sift.modules.bookmark;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
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
}
