package com.sift.modules.tag;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping("/tags/create")
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponseDTO createTag(
            Authentication authentication,
            @RequestBody CreateTagRequestDTO request
    ) {
        return tagService.createTag(
                authentication,
                request
        );
    }

    @GetMapping("/tags")
    public List<TagResponseDTO> getTags(
            Authentication authentication
    ) {
        return tagService.getUserTags(authentication);
    }

    @PostMapping("/bookmarks/{bookmarkId}/tags")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addTag(
            Authentication authentication,
            @PathVariable UUID bookmarkId,
            @RequestBody AddTagToBookmarkRequestDTO request
    ) {
        tagService.addTagToBookmark(
                authentication,
                bookmarkId,
                request.tagId()
        );
    }

    @DeleteMapping("/bookmarks/{bookmarkId}/tags/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTag(
            Authentication authentication,
            @PathVariable UUID bookmarkId,
            @PathVariable UUID tagId
    ) {
        tagService.removeTagFromBookmark(
                authentication,
                bookmarkId,
                tagId
        );
    }

    @GetMapping("/bookmarks/{bookmarkId}/tags")
    public List<TagResponseDTO> getBookmarkTags(
            Authentication authentication,
            @PathVariable UUID bookmarkId
    ) {
        return tagService.getBookmarkTags(
                authentication,
                bookmarkId
        );
    }
}