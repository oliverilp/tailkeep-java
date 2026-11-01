package org.tailkeep.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.tailkeep.api.model.auth.JwtSecret;

import java.util.Optional;
import java.util.UUID;

public interface JwtSecretRepository extends JpaRepository<JwtSecret, UUID> {
    @Query("SELECT js FROM JwtSecret js ORDER BY js.createdAt DESC LIMIT 1")
    Optional<JwtSecret> findLatestSecret();
} 
