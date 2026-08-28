package com.sift.modules.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookmarkTagRepository
        extends JpaRepository<
        BookmarkTagEntity,
        BookmarkTagEntity.BookmarkTagId> {

    boolean existsByBookmarkIdAndTagId(
            UUID bookmarkId,
            UUID tagId
    );

    List<BookmarkTagEntity> findAllByBookmarkId(
            UUID bookmarkId
    );

    List<BookmarkTagEntity> findAllByBookmarkIdIn(
            List<UUID> bookmarkIds
    );

    void deleteByBookmarkIdAndTagId(
            UUID bookmarkId,
            UUID tagId
    );

    void deleteAllByBookmarkId(
            UUID bookmarkId
    );
}