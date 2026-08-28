package com.sift.modules.bookmark;

import com.sift.modules.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
