package com.sift.modules.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookmarkRepository extends JpaRepository<BookmarkEntity, UUID> {

    boolean existsByUserIdAndTweetId(UUID userId, UUID tweetId);
}
