package com.sift.modules.bookmark;

import com.sift.modules.collection.CollectionEntity;
import com.sift.modules.note.BookmarkNoteEntity;
import com.sift.modules.tag.TagEntity;
import com.sift.modules.tweet.TweetEntity;
import com.sift.modules.user.UserEntity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "bookmarks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bookmarks_user_tweet",
                        columnNames = {"user_id", "tweet_id"}
                )
        }
)
public class BookmarkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_bookmarks_user"
            )
    )
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "tweet_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_bookmarks_tweet"
            )
    )
    private TweetEntity tweet;

    /*
     * NULL means the bookmark is in Inbox.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "collection_id",
            foreignKey = @ForeignKey(
                    name = "fk_bookmarks_collection"
            )
    )
    private CollectionEntity collection;

    @Column(
            name = "is_favorite",
            nullable = false
    )
    private boolean favorite = false;

    @Column(
            name = "is_read",
            nullable = false
    )
    private boolean read = false;

    @Column(
            name = "saved_at",
            nullable = false,
            insertable = false
    )
    private OffsetDateTime savedAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "bookmark_tags",
            joinColumns = @JoinColumn(name = "bookmark_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<TagEntity> tags = new ArrayList<>();

    @OneToOne(
            mappedBy = "bookmark",
            fetch = FetchType.LAZY
    )
    private BookmarkNoteEntity note;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        savedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public TweetEntity getTweet() {
        return tweet;
    }

    public void setTweet(TweetEntity tweet) {
        this.tweet = tweet;
    }

    public CollectionEntity getCollection() {
        return collection;
    }

    public void setCollection(CollectionEntity collection) {
        this.collection = collection;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public OffsetDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(OffsetDateTime savedAt) {
        this.savedAt = savedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TagEntity> getTags() {
        return tags;
    }

    public void setTags(List<TagEntity> tags) {
        this.tags = tags;
    }

    public BookmarkNoteEntity getNote() {
        return note;
    }

    public void setNote(BookmarkNoteEntity note) {
        this.note = note;
    }
}