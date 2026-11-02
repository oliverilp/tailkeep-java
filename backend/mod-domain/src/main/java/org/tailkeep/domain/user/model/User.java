package org.tailkeep.domain.user.model;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Someone who can log in, and the aggregate root of their {@link org.tailkeep.domain.auth.model.Token tokens}. The
 * tokens are not held here, because a user collects them for ever and most use cases don't need them. {@code id}
 * is null until the user is saved.
 */
@Builder(toBuilder = true)
public record User(
        UUID id,
        String nickname,
        String username,
        String passwordHash,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    /** In demo mode everyone shares the demo account, so only an admin may change a password. */
    public boolean mayChangePassword(boolean demoMode) {
        return !demoMode || role == Role.ADMIN;
    }

    public User withPasswordHash(String passwordHash) {
        return toBuilder().passwordHash(passwordHash).build();
    }
}
