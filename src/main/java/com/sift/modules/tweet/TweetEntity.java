package com.sift.modules.tweet;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "tweets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tweets_tweet_id",
                        columnNames = "tweet_id"
                ),
                @UniqueConstraint(
                        name = "uk_tweets_url",
                        columnNames = "url"
                )
        }
)
public class TweetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "tweet_id",
            nullable = false,
            length = 50
    )
    private String tweetId;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String url;

    @Column(name = "author_username")
    private String authorUsername;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "author_avatar_url")
    private String authorAvatarUrl;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(
            name = "fetched_at",
            nullable = false,
            insertable = false
    )
    private OffsetDateTime fetchedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTweetId() {
        return tweetId;
    }

    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(OffsetDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}