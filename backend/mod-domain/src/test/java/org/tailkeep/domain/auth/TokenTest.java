package org.tailkeep.domain.auth;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TokenTest {
    private final Token token = Token.issuedTo(UUID.randomUUID(), "value");

    @Test
    void aNewTokenIsAnActiveBearerToken() {
        assertThat(token.isActive()).isTrue();
        assertThat(token.type()).isEqualTo(TokenType.BEARER);
    }

    @Test
    void revokingSetsBothFlags() {
        Token revoked = token.revoke();

        assertThat(revoked.revoked()).isTrue();
        assertThat(revoked.expired()).isTrue();
        assertThat(revoked.isActive()).isFalse();
    }

    @Test
    void eitherFlagMakesATokenInactive() {
        assertThat(token.toBuilder().revoked(true).build().isActive()).isFalse();
        assertThat(token.toBuilder().expired(true).build().isActive()).isFalse();
    }
}
