package com.sift.modules.collection;

import com.sift.modules.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CollectionRepository
        extends JpaRepository<CollectionEntity, UUID> {

    Optional<CollectionEntity> findByIdAndUser(
            UUID id,
            UserEntity user
    );
}
