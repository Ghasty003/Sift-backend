package com.sift.modules.tag;

import com.sift.modules.bookmark.BookmarkEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "bookmark_tags")
@IdClass(BookmarkTagEntity.BookmarkTagId.class)
public class BookmarkTagEntity {

    @Id
    @Column(name = "bookmark_id")
    private UUID bookmarkId;

    @Id
    @Column(name = "tag_id")
    private UUID tagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "bookmark_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_bookmark_tags_bookmark")
    )
    private BookmarkEntity bookmark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tag_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_bookmark_tags_tag")
    )
    private TagEntity tag;

    public BookmarkTagEntity() {
    }

    public BookmarkTagEntity(UUID bookmarkId, UUID tagId) {
        this.bookmarkId = bookmarkId;
        this.tagId = tagId;
    }

    public UUID getBookmarkId() {
        return bookmarkId;
    }

    public UUID getTagId() {
        return tagId;
    }

    public BookmarkEntity getBookmark() {
        return bookmark;
    }

    public TagEntity getTag() {
        return tag;
    }

    public static class BookmarkTagId implements Serializable {

        private UUID bookmarkId;
        private UUID tagId;

        public BookmarkTagId() {
        }

        public BookmarkTagId(UUID bookmarkId, UUID tagId) {
            this.bookmarkId = bookmarkId;
            this.tagId = tagId;
        }

        public UUID getBookmarkId() {
            return bookmarkId;
        }

        public UUID getTagId() {
            return tagId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BookmarkTagId that)) return false;

            return bookmarkId.equals(that.bookmarkId)
                    && tagId.equals(that.tagId);
        }

        @Override
        public int hashCode() {
            return 31 * bookmarkId.hashCode() + tagId.hashCode();
        }
    }
}