package com.sift.modules.collection;

import com.sift.exceptions.ResourceNotFoundException;
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
        UserEntity user = (UserEntity) authentication.getPrincipal();

        CollectionEntity collection = new CollectionEntity();
        collection.setUser(user);
        collection.setName(request.name());
        collection.setDescription(request.description());

        CollectionEntity saved = collectionRepository.save(collection);

        // Freshly created — no bookmarks in it yet, no query needed.
        return toResponse(saved, 0, 0);
    }

    @Transactional(readOnly = true)
    public List<CollectionResponseDTO> getCollections(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        List<CollectionEntity> collections =
                collectionRepository.findAllByUserOrderByCreatedAtDesc(user);

        Map<UUID, BookmarkRepository.CollectionBookmarkStats> statsByCollection =
                bookmarkRepository.countByCollectionForUser(user.getId()).stream()
                        .collect(Collectors.toMap(
                                BookmarkRepository.CollectionBookmarkStats::getCollectionId,
                                Function.identity()
                        ));

        return collections.stream()
                .map(c -> {
                    BookmarkRepository.CollectionBookmarkStats stats =
                            statsByCollection.get(c.getId());
                    long total = stats != null ? stats.getTotal() : 0;
                    long unread = stats != null ? stats.getUnread() : 0;
                    return toResponse(c, total, unread);
                })
                .toList();
    }

    @Transactional
    public void deleteCollection(Authentication authentication, UUID collectionId) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        CollectionEntity collection = collectionRepository
                .findByIdAndUser(collectionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        bookmarkRepository.moveBookmarksToInbox(collection);
        collectionRepository.delete(collection);
    }

    private CollectionResponseDTO toResponse(
            CollectionEntity collection,
            long bookmarkCount,
            long unreadCount
    ) {
        return new CollectionResponseDTO(
                collection.getId(),
                collection.getName(),
                collection.getDescription(),
                collection.getCreatedAt(),
                collection.getUpdatedAt(),
                bookmarkCount,
                unreadCount
        );
    }
}