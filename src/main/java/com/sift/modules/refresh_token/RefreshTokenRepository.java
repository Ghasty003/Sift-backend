package com.sift.modules.refresh_token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
    List<RefreshTokenEntity> findByFamilyIdAndRevokedAtIsNull(UUID familyId);
    List<RefreshTokenEntity> findByUserIdAndRevokedAtIsNull(UUID userId);
}