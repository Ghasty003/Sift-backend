package com.sift.modules.note;

import com.sift.modules.bookmark.BookmarkEntity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "bookmark_notes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bookmark_notes_bookmark",
                        columnNames = "bookmark_id"
                )
        }
)
public class BookmarkNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bookmark_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_bookmark_notes_bookmark")
    )
    private BookmarkEntity bookmark;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public BookmarkEntity getBookmark() {
        return bookmark;
    }

    public void setBookmark(BookmarkEntity bookmark) {
        this.bookmark = bookmark;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}