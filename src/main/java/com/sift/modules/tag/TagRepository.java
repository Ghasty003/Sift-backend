package com.sift.modules.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<TagEntity, UUID> {

    Optional<TagEntity> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    Optional<TagEntity> findByUserIdAndName(
            UUID userId,
            String name
    );

    List<TagEntity> findAllByUserIdOrderByNameAsc(
            UUID userId
    );
}