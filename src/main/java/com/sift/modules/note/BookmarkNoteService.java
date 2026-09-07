package com.sift.modules.note;

import com.sift.exceptions.BadRequestException;
import com.sift.exceptions.ResourceNotFoundException;
import com.sift.modules.bookmark.BookmarkEntity;
import com.sift.modules.bookmark.BookmarkRepository;
import com.sift.modules.user.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookmarkNoteService {

    private final BookmarkNoteRepository noteRepository;
    private final BookmarkRepository bookmarkRepository;

    public BookmarkNoteService(
            BookmarkNoteRepository noteRepository,
            BookmarkRepository bookmarkRepository
    ) {
        this.noteRepository = noteRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    @Transactional
    public NoteResponseDTO createOrUpdateNote(
            Authentication authentication,
            UUID bookmarkId,
            CreateOrUpdateNoteRequestDTO request
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        BookmarkEntity bookmark = bookmarkRepository
                .findByIdAndUserId(bookmarkId, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bookmark not found")
                );

        String content = request.content().trim();

        if (content.isBlank()) {
            throw new BadRequestException(
                    "Note content cannot be empty"
            );
        }

        BookmarkNoteEntity note = noteRepository
                .findByBookmarkId(bookmarkId)
                .orElseGet(() -> {
                    BookmarkNoteEntity newNote =
                            new BookmarkNoteEntity();

                    newNote.setBookmark(bookmark);

                    return newNote;
                });

        note.setContent(content);

        note = noteRepository.save(note);

        return new NoteResponseDTO(
                note.getId(),
                bookmarkId,
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public NoteResponseDTO getNote(
            Authentication authentication,
            UUID bookmarkId
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        bookmarkRepository
                .findByIdAndUserId(bookmarkId, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Bookmark not found")
                );

        return noteRepository
                .findByBookmarkId(bookmarkId)
                .map(note -> new NoteResponseDTO(
                        note.getId(),
                        bookmarkId,
                        note.getContent(),
                        note.getCreatedAt(),
                        note.getUpdatedAt()
                ))
                .orElse(null);
    }

    @Transactional
    public void deleteNote(
            Authentication authentication,
            UUID bookmarkId
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        bookmarkRepository
                .findByIdAndUserId(bookmarkId, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Bookmark not found")
                );

        noteRepository.deleteByBookmarkId(bookmarkId);
    }
}