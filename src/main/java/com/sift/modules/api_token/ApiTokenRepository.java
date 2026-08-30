package com.sift.modules.api_token;

import com.sift.modules.user.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiTokenRepository
        extends JpaRepository<ApiTokenEntity, UUID> {

    @EntityGraph(attributePaths = "user")
    Optional<ApiTokenEntity> findByTokenIdAndRevokedAtIsNull(
            String tokenHash
    );

    Optional<ApiTokenEntity> findByTokenIdAndUser(
            String tokenId,
            UserEntity user
    );

    List<ApiTokenEntity> findAllByUserOrderByCreatedAtDesc(
            UserEntity user
    );
}
