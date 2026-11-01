package org.tailkeep.domain.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class FakeTokens implements FindToken, SaveToken {
    final List<Token> saved = new ArrayList<>();

    @Override
    public Optional<Token> byValue(String value) {
        return saved.stream().filter(token -> token.value().equals(value)).findFirst();
    }

    @Override
    public List<Token> notRevokedByUser(UUID userId) {
        return saved.stream()
                .filter(token -> token.userId().equals(userId) && (!token.expired() || !token.revoked()))
                .toList();
    }

    @Override
    public void save(Token token) {
        Token stored = token.id() != null ? token : token.toBuilder().id(UUID.randomUUID()).build();
        saved.removeIf(existing -> existing.id().equals(stored.id()));
        saved.add(stored);
    }

    @Override
    public void saveAll(List<Token> tokens) {
        tokens.forEach(this::save);
    }
}
