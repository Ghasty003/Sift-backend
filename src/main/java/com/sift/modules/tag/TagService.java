package com.sift.modules.tag;

import com.sift.modules.bookmark.BookmarkEntity;
import com.sift.modules.bookmark.BookmarkRepository;
import com.sift.modules.user.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final BookmarkRepository bookmarkRepository;
    private final BookmarkTagRepository bookmarkTagRepository;

    public TagService(
            TagRepository tagRepository,
            BookmarkRepository bookmarkRepository,
            BookmarkTagRepository bookmarkTagRepository
    ) {
        this.tagRepository = tagRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.bookmarkTagRepository = bookmarkTagRepository;
    }

    public TagResponseDTO createTag(
            Authentication authentication,
            CreateTagRequestDTO request
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        String name = request.name().trim();

        if (name.isBlank()) {
            throw new IllegalArgumentException("Tag name cannot be empty");
        }

        assert user != null;
        TagEntity tag = tagRepository
                .findByUserIdAndName(user.getId(), name)
                .orElseGet(() -> {
                    TagEntity newTag = new TagEntity();
                    newTag.setUser(user);
                    newTag.setName(name);

                    return tagRepository.save(newTag);
                });

        return new TagResponseDTO(
                tag.getId(),
                tag.getName()
        );
    }

    @Transactional(readOnly = true)
    public List<TagResponseDTO> getUserTags(
            Authentication authentication
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        return tagRepository
                .findAllByUserIdOrderByNameAsc(user.getId())
                .stream()
                .map(tag -> new TagResponseDTO(
                        tag.getId(),
                        tag.getName()
                ))
                .toList();
    }

    @Transactional
    public void addTagToBookmark(
            Authentication authentication,
            UUID bookmarkId,
            UUID tagId
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        BookmarkEntity bookmark = bookmarkRepository
                .findByIdAndUserId(bookmarkId, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Bookmark not found")
                );

        TagEntity tag = tagRepository
                .findByIdAndUserId(tagId, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Tag not found")
                );

        if (bookmarkTagRepository
                .existsByBookmarkIdAndTagId(bookmark.getId(), tag.getId())) {

            return;
        }

        BookmarkTagEntity bookmarkTag =
                new BookmarkTagEntity(
                        bookmark.getId(),
                        tag.getId()
                );

        bookmarkTagRepository.save(bookmarkTag);
    }

    @Transactional
    public void removeTagFromBookmark(
            Authentication authentication,
            UUID bookmarkId,
            UUID tagId
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        bookmarkRepository
                .findByIdAndUserId(bookmarkId, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Bookmark not found")
                );

        tagRepository
                .findByIdAndUserId(tagId, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Tag not found")
                );

        bookmarkTagRepository.deleteByBookmarkIdAndTagId(
                bookmarkId,
                tagId
        );
    }

    @Transactional(readOnly = true)
    public List<TagResponseDTO> getBookmarkTags(
            Authentication authentication,
            UUID bookmarkId
    ) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        bookmarkRepository
                .findByIdAndUserId(bookmarkId, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Bookmark not found")
                );

        return bookmarkTagRepository
                .findAllByBookmarkId(bookmarkId)
                .stream()
                .map(bookmarkTag -> bookmarkTag.getTag())
                .map(tag -> new TagResponseDTO(
                        tag.getId(),
                        tag.getName()
                ))
                .toList();
    }
}