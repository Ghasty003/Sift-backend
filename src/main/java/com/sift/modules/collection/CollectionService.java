package com.sift.modules.collection;

import com.sift.exceptions.ResourceNotFoundException;
import com.sift.modules.bookmark.BookmarkRepository;
import com.sift.modules.user.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final BookmarkRepository bookmarkRepository;

    public CollectionService(CollectionRepository collectionRepository, BookmarkRepository bookmarkRepository) {
        this.collectionRepository = collectionRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    @Transactional
    public CollectionResponseDTO createCollection(
            Authentication authentication,
            CreateCollectionRequestDTO request
    ) {
        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        CollectionEntity collection = new CollectionEntity();

        collection.setUser(user);
        collection.setName(request.name());
        collection.setDescription(request.description());

        CollectionEntity saved =
                collectionRepository.save(collection);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CollectionResponseDTO> getCollections(
            Authentication authentication
    ) {
        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        return collectionRepository
                .findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteCollection(
            Authentication authentication,
            UUID collectionId
    ) {
        UserEntity user =
                (UserEntity) authentication.getPrincipal();

        CollectionEntity collection =
                collectionRepository
                        .findByIdAndUser(collectionId, user)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Collection not found"
                                )
                        );

        // Move all bookmarks in this collection
        // back to the Inbox.
        bookmarkRepository.moveBookmarksToInbox(collection);

        // Now the collection can safely be deleted.
        collectionRepository.delete(collection);
    }

    private CollectionResponseDTO toResponse(
            CollectionEntity collection
    ) {
        return new CollectionResponseDTO(
                collection.getId(),
                collection.getName(),
                collection.getDescription(),
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }
}