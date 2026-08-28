package com.sift.modules.collection;

import com.sift.modules.user.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;

    public CollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

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

        return toResponse(saved);
    }

    private CollectionResponseDTO toResponse(CollectionEntity collection) {
        return new CollectionResponseDTO(
                collection.getId(),
                collection.getName(),
                collection.getDescription(),
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }
}