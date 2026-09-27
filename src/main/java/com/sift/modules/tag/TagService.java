package com.sift.modules.tag;

import com.sift.exceptions.BadRequestException;
import com.sift.exceptions.ResourceNotFoundException;
import com.sift.modules.bookmark.BookmarkEntity;
import com.sift.modules.bookmark.BookmarkRepository;
import com.sift.modules.user.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public TagResponseDTO createTag(Authentication authentication, CreateTagRequestDTO request) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        String name = request.name().trim();
        if (name.isBlank()) {
            throw new BadRequestException("Tag name cannot be empty");
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

        // bookmarkCount isn't meaningful for a just-created/looked-up tag in
        // this context (this DTO here isn't used to render a count anywhere),
        // so 0 is a safe placeholder rather than an extra query.
        return new TagResponseDTO(tag.getId(), tag.getName(), 0);
    }

    @Transactional(readOnly = true)
    public List<TagResponseDTO> getUserTags(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        List<TagEntity> tags = tagRepository.findAllByUserIdOrderByNameAsc(user.getId());

        Map<UUID, Long> countsByTag = bookmarkTagRepository.countByTagForUser(user.getId()).stream()
                .collect(Collectors.toMap(
                        BookmarkTagRepository.TagBookmarkStats::getTagId,
                        BookmarkTagRepository.TagBookmarkStats::getTotal
                ));

        return tags.stream()
                .map(tag -> new TagResponseDTO(
                        tag.getId(),
                        tag.getName(),
                        countsByTag.getOrDefault(tag.getId(), 0L)
                ))
                .toList();
    }

    @Transactional
    public void addTagToBookmark(Authentication authentication, UUID bookmarkId, UUID tagId) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        BookmarkEntity bookmark = bookmarkRepository
                .findByIdAndUserId(bookmarkId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));

        TagEntity tag = tagRepository
                .findByIdAndUserId(tagId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        if (bookmarkTagRepository.existsByBookmarkIdAndTagId(bookmark.getId(), tag.getId())) {
            return;
        }

        bookmarkTagRepository.save(new BookmarkTagEntity(bookmark.getId(), tag.getId()));
    }

    @Transactional
    public void removeTagFromBookmark(Authentication authentication, UUID bookmarkId, UUID tagId) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        bookmarkRepository
                .findByIdAndUserId(bookmarkId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));

        tagRepository
                .findByIdAndUserId(tagId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        bookmarkTagRepository.deleteByBookmarkIdAndTagId(bookmarkId, tagId);
    }

    @Transactional(readOnly = true)
    public List<TagResponseDTO> getBookmarkTags(Authentication authentication, UUID bookmarkId) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        bookmarkRepository
                .findByIdAndUserId(bookmarkId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));

        return bookmarkTagRepository
                .findAllByBookmarkId(bookmarkId)
                .stream()
                .map(BookmarkTagEntity::getTag)
                // bookmarkCount isn't relevant to "tags on this one bookmark" —
                // same 0 placeholder as createTag, for the same reason.
                .map(tag -> new TagResponseDTO(tag.getId(), tag.getName(), 0))
                .toList();
    }
}