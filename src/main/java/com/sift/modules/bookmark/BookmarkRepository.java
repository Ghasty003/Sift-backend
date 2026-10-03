package com.sift.modules.bookmark;

import com.sift.modules.collection.CollectionEntity;
import com.sift.modules.user.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, UUID> {

    Optional<BookmarkEntity> findByUser_IdAndTweet_Id(UUID userId, UUID tweetId);

    Optional<BookmarkEntity> findByIdAndUser(UUID id, UserEntity user);

    Optional<BookmarkEntity> findByIdAndUserId(UUID bookmarkId, UUID userId);

    @Modifying
    @Query("""
        UPDATE BookmarkEntity b
        SET b.collection = null
        WHERE b.collection = :collection
        """)
    void moveBookmarksToInbox(@Param("collection") CollectionEntity collection);

    long countByUser_IdAndCollectionIsNull(UUID userId);

    long countByUser_IdAndFavoriteTrue(UUID userId);

    List<BookmarkEntity> findTop5ByUser_IdOrderBySavedAtDesc(UUID userId);

    /*
     * Single query backing every list view (Dashboard's recent list aside,
     * which stays on findTop5). Keyset (cursor) pagination on (savedAt, id)
     * rather than OFFSET/LIMIT, since offset pagination degrades on large
     * tables and shifts under concurrent inserts; keyset stays correct and
     * fast regardless of how deep the user scrolls.
     *
     * Every filter parameter is optional: a null/false value makes its
     * clause a no-op via the "(:param IS NULL OR ...)" pattern, so this one
     * query serves the inbox, favorites, unread, per-collection, and
     * fully-filtered "all bookmarks" views alike.
     */
    @Query("""
        SELECT DISTINCT b FROM BookmarkEntity b
        LEFT JOIN b.tags t
        LEFT JOIN b.note n
        WHERE b.user.id = :userId
          AND (:inboxOnly = false OR b.collection IS NULL)
          AND (:hasCollectionId = false OR b.collection.id = :collectionId)
          AND (:favoriteOnly = false OR b.favorite = true)
          AND (:hasReadFilter = false OR b.read = :readFilter)
          AND (:hasTagId = false OR t.id = :tagId)
          AND (
              :hasSearch = false
              OR LOWER(b.tweet.text) LIKE :search
              OR LOWER(b.tweet.authorName) LIKE :search
              OR LOWER(b.tweet.authorUsername) LIKE :search
              OR LOWER(n.content) LIKE :search
              OR LOWER(t.name) LIKE :search
          )
          AND (
              :hasCursor = false
              OR b.savedAt < :cursorSavedAt
              OR (b.savedAt = :cursorSavedAt AND b.id < :cursorId)
          )
        ORDER BY b.savedAt DESC, b.id DESC
        """)
    List<BookmarkEntity> searchBookmarks(
            @Param("userId") UUID userId,
            @Param("inboxOnly") boolean inboxOnly,
            @Param("hasCollectionId") boolean hasCollectionId,
            @Param("collectionId") UUID collectionId,
            @Param("favoriteOnly") boolean favoriteOnly,
            @Param("hasReadFilter") boolean hasReadFilter,
            @Param("readFilter") boolean readFilter,
            @Param("hasTagId") boolean hasTagId,
            @Param("tagId") UUID tagId,
            @Param("hasSearch") boolean hasSearch,
            @Param("search") String search,
            @Param("hasCursor") boolean hasCursor,
            @Param("cursorSavedAt") OffsetDateTime cursorSavedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable
    );

    // Dashboard/aggregation support

    long countByUser_Id(UUID userId);

    long countByUser_IdAndReadFalse(UUID userId);

    List<BookmarkEntity> findTop6ByUser_IdOrderBySavedAtDesc(UUID userId);

    interface CollectionBookmarkStats {
        UUID getCollectionId();
        long getTotal();
        long getUnread();
    }

    @Query("""
        SELECT b.collection.id AS collectionId,
               COUNT(b) AS total,
               SUM(CASE WHEN b.read = false THEN 1L ELSE 0L END) AS unread
        FROM BookmarkEntity b
        WHERE b.user.id = :userId AND b.collection IS NOT NULL
        GROUP BY b.collection.id
        """)
    List<CollectionBookmarkStats> countByCollectionForUser(@Param("userId") UUID userId);

    @Modifying
    @Query("""
        UPDATE BookmarkEntity b
        SET b.read = true
        WHERE b.user.id = :userId AND b.read = false
        """)
    int markAllAsRead(@Param("userId") UUID userId);

    @Query("""
        SELECT DISTINCT b FROM BookmarkEntity b
        LEFT JOIN b.tags t
        LEFT JOIN b.note n
        WHERE b.user.id = :userId
          AND (:inboxOnly = false OR b.collection IS NULL)
          AND (:hasCollectionId = false OR b.collection.id = :collectionId)
          AND (:favoriteOnly = false OR b.favorite = true)
          AND (:hasReadFilter = false OR b.read = :readFilter)
          AND (:hasTagId = false OR t.id = :tagId)
          AND (
              :hasSearch = false
              OR LOWER(b.tweet.text) LIKE :search
              OR LOWER(b.tweet.authorName) LIKE :search
              OR LOWER(b.tweet.authorUsername) LIKE :search
              OR LOWER(n.content) LIKE :search
              OR LOWER(t.name) LIKE :search
          )
          AND (
              :hasCursor = false
              OR b.savedAt > :cursorSavedAt
              OR (b.savedAt = :cursorSavedAt AND b.id > :cursorId)
          )
        ORDER BY b.savedAt ASC, b.id ASC
        """)
    List<BookmarkEntity> searchBookmarksAscending(
            @Param("userId") UUID userId,
            @Param("inboxOnly") boolean inboxOnly,
            @Param("hasCollectionId") boolean hasCollectionId,
            @Param("collectionId") UUID collectionId,
            @Param("favoriteOnly") boolean favoriteOnly,
            @Param("hasReadFilter") boolean hasReadFilter,
            @Param("readFilter") boolean readFilter,
            @Param("hasTagId") boolean hasTagId,
            @Param("tagId") UUID tagId,
            @Param("hasSearch") boolean hasSearch,
            @Param("search") String search,
            @Param("hasCursor") boolean hasCursor,
            @Param("cursorSavedAt") OffsetDateTime cursorSavedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM BookmarkEntity b WHERE b.id IN :ids AND b.user.id = :userId")
    int deleteAllByIdInAndUserId(@Param("ids") List<UUID> ids, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE BookmarkEntity b SET b.collection.id = :collectionId WHERE b.id IN :ids AND b.user.id = :userId")
    int moveAllByIdInAndUserId(@Param("ids") List<UUID> ids, @Param("collectionId") UUID collectionId, @Param("userId") UUID userId);
}
