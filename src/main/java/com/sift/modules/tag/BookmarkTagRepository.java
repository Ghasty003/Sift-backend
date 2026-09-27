package com.sift.modules.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    interface TagBookmarkStats {
        UUID getTagId();
        long getTotal();
    }

    @Query("""
        SELECT bt.tag.id AS tagId, COUNT(bt) AS total
        FROM BookmarkTagEntity bt
        WHERE bt.bookmark.user.id = :userId
        GROUP BY bt.tag.id
        """)
    List<TagBookmarkStats> countByTagForUser(@Param("userId") UUID userId);
}