package com.sift.modules.api_token;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiTokenRepository
        extends JpaRepository<ApiTokenEntity, UUID> {

    @EntityGraph(attributePaths = "user")
    Optional<ApiTokenEntity> findByTokenIdAndRevokedAtIsNull(
            String tokenHash
    );
}
