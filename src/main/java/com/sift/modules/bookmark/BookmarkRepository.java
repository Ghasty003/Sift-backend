package com.sift.modules.bookmark;

import com.sift.modules.collection.CollectionEntity;
import com.sift.modules.user.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, UUID> {

    boolean existsByUser_IdAndTweet_Id(
            UUID userId,
            UUID tweetId
    );

    Optional<BookmarkEntity> findByIdAndUser(
            UUID id,
            UserEntity user
    );

    Optional<BookmarkEntity> findByIdAndUserId(
            UUID bookmarkId,
            UUID userId
    );

    @EntityGraph(attributePaths = {
            "tweet",
            "collection"
    })
    List<BookmarkEntity> findAllByUserIdOrderBySavedAtDesc(
            UUID userId
    );

    List<BookmarkEntity> findAllByUserAndCollectionOrderBySavedAtDesc(
            UserEntity user,
            CollectionEntity collection
    );

    List<BookmarkEntity> findAllByUserIdAndCollectionIsNullOrderBySavedAtDesc( UUID userId );
    List<BookmarkEntity> findAllByUserIdAndFavoriteTrueOrderBySavedAtDesc( UUID userId );
    List<BookmarkEntity> findAllByUserIdAndReadFalseOrderBySavedAtDesc( UUID userId );

    @Modifying
    @Query("""
        UPDATE BookmarkEntity b
        SET b.collection = null
        WHERE b.collection = :collection
        """)
    void moveBookmarksToInbox(
            @Param("collection") CollectionEntity collection
    );

}
