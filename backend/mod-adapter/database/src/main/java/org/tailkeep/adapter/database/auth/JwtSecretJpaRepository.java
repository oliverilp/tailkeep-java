package org.tailkeep.adapter.database.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JwtSecretJpaRepository extends JpaRepository<JwtSecretEntity, UUID> {
    @Query("SELECT js FROM JwtSecretEntity js ORDER BY js.createdAt DESC LIMIT 1")
    Optional<JwtSecretEntity> findLatestSecret();
}
