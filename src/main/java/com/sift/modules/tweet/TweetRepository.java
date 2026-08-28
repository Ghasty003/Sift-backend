package com.sift.modules.tweet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TweetRepository extends JpaRepository<TweetEntity, UUID> {

    Optional<TweetEntity> findByTweetId(String tweetId);
}
