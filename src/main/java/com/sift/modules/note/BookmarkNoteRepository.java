package com.sift.modules.note;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookmarkNoteRepository
        extends JpaRepository<BookmarkNoteEntity, UUID> {

    Optional<BookmarkNoteEntity> findByBookmarkId(
            UUID bookmarkId
    );

    void deleteByBookmarkId(
            UUID bookmarkId
    );

    List<BookmarkNoteEntity> findAllByBookmarkIdIn(
            List<UUID> bookmarkIds
    );
}