package com.sift.modules.note;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookmarks/{bookmarkId}/note")
public class BookmarkNoteController {

    private final BookmarkNoteService noteService;

    public BookmarkNoteController(BookmarkNoteService noteService) {
        this.noteService = noteService;
    }

    @PutMapping
    public NoteResponseDTO createOrUpdateNote(
            Authentication authentication,
            @PathVariable UUID bookmarkId,
            @RequestBody CreateOrUpdateNoteRequestDTO request
    ) {
        return noteService.createOrUpdateNote(
                authentication,
                bookmarkId,
                request
        );
    }

    @GetMapping
    public NoteResponseDTO getNote(
            Authentication authentication,
            @PathVariable UUID bookmarkId
    ) {
        return noteService.getNote(
                authentication,
                bookmarkId
        );
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(
            Authentication authentication,
            @PathVariable UUID bookmarkId
    ) {
        noteService.deleteNote(
                authentication,
                bookmarkId
        );
    }
}