package org.tailkeep.adapter.database.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenJpaRepository extends JpaRepository<TokenEntity, UUID> {
    @Query("select t from TokenEntity t where t.userId = :userId and (t.expired = false or t.revoked = false)")
    List<TokenEntity> findNotRevokedByUser(@Param("userId") UUID userId);

    Optional<TokenEntity> findByToken(String token);
}
