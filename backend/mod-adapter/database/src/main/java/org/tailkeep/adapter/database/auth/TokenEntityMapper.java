package org.tailkeep.adapter.database.auth;

import org.tailkeep.domain.auth.model.Token;

public final class TokenEntityMapper {
    private TokenEntityMapper() {
    }

    public static Token toDomain(TokenEntity entity) {
        return Token.builder()
                .id(entity.getId())
                .value(entity.getToken())
                .type(entity.getTokenType())
                .revoked(entity.isRevoked())
                .expired(entity.isExpired())
                .userId(entity.getUserId())
                .build();
    }

    public static TokenEntity toEntity(Token token) {
        return TokenEntity.builder()
                .id(token.id())
                .token(token.value())
                .tokenType(token.type())
                .revoked(token.revoked())
                .expired(token.expired())
                .userId(token.userId())
                .build();
    }
}
