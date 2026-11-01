package org.tailkeep.domain.auth;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.download.FakeRunInTransaction;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LogoutImplTest {
    private static final String TOKEN = "access:someone:0";

    private final FakeTokens tokens = new FakeTokens();
    private final Logout logout =
            new LogoutImpl(tokens, tokens, new FakeTokenIssuer(), new FakeRunInTransaction());

    @Test
    void revokesAnActiveToken() {
        tokens.save(Token.issuedTo(UUID.randomUUID(), TOKEN));

        assertThat(logout.execute(TOKEN)).isTrue();
        assertThat(tokens.byValue(TOKEN).orElseThrow().isActive()).isFalse();
    }

    @Test
    void refusesARevokedToken() {
        tokens.save(Token.issuedTo(UUID.randomUUID(), TOKEN).revoke());

        assertThat(logout.execute(TOKEN)).isFalse();
    }

    @Test
    void refusesAValidTokenThatIsNotStored() {
        assertThat(logout.execute(TOKEN)).isFalse();
    }

    @Test
    void refusesAnInvalidToken() {
        assertThat(logout.execute("garbage")).isFalse();
    }
}
