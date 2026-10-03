package com.sift.modules.tweet;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "tweet_media",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tweet_media_tweet_position",
                columnNames = {"tweet_id", "position"}
        )
)
public class TweetMediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "tweet_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_tweet_media_tweet")
    )
    private TweetEntity tweet;

    @Column(name = "media_type", nullable = false, length = 20)
    private String mediaType;

    @Column(name = "preview_url", nullable = false, columnDefinition = "TEXT")
    private String previewUrl;

    @Column(nullable = false)
    private int position;

    public UUID getId() { return id; }
    public TweetEntity getTweet() { return tweet; }
    public void setTweet(TweetEntity tweet) { this.tweet = tweet; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
