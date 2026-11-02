package org.tailkeep.domain.auth.model;

import lombok.Builder;

import java.util.UUID;

/**
 * An access token that was given to a user, part of the {@link org.tailkeep.domain.user.model.User} aggregate. Only
 * access tokens are stored. {@code id} is null until the token is saved.
 */
@Builder(toBuilder = true)
public record Token(
        UUID id,
        String value,
        TokenType type,
        boolean revoked,
        boolean expired,
        UUID userId) {

    public static Token issuedTo(UUID userId, String value) {
        return Token.builder().userId(userId).value(value).type(TokenType.BEARER).build();
    }

    public boolean isActive() {
        return !expired && !revoked;
    }

    public Token revoke() {
        return toBuilder().expired(true).revoked(true).build();
    }
}
