package com.sift.modules.bookmark;


import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}
