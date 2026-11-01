package org.tailkeep.domain.auth;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.download.FakeRunInTransaction;
import org.tailkeep.domain.user.FakePasswordHasher;
import org.tailkeep.domain.user.FakeUsers;
import org.tailkeep.domain.user.Role;
import org.tailkeep.domain.user.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticateImplTest {
    private static final String PASSWORD = "password12345";

    private final FakeUsers users = new FakeUsers();
    private final FakeTokens tokens = new FakeTokens();
    private final FakeTokenIssuer issuer = new FakeTokenIssuer();
    private final FakePasswordHasher hasher = new FakePasswordHasher();
    private final Authenticate authenticate = new AuthenticateImpl(users, hasher,
            new IssueTokensImpl(tokens, tokens, issuer, new FakeRunInTransaction()));

    private void assertRefused(String username, String password) {
        assertThatThrownBy(() -> authenticate.execute(username, password))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid credentials provided");
    }

    @Test
    void returnsTokensAndStoresTheAccessToken() {
        User user = users.add("someone", "hashed:" + PASSWORD, Role.USER);

        AuthTokens result = authenticate.execute("someone", PASSWORD);

        assertThat(result.accessToken()).startsWith("access:someone");
        assertThat(result.refreshToken()).startsWith("refresh:someone");
        Token stored = tokens.byValue(result.accessToken()).orElseThrow();
        assertThat(stored.isActive()).isTrue();
        assertThat(stored.userId()).isEqualTo(user.id());
    }

    @Test
    void refusesAWrongPasswordAndAnUnknownUserWithTheSameMessage() {
        users.add("someone", "hashed:" + PASSWORD, Role.USER);

        assertRefused("someone", "wrongPassword");
        assertRefused("nobody", PASSWORD);
        assertThat(tokens.saved).isEmpty();
    }

    @Test
    void refusesAMissingPassword() {
        users.add("someone", "hashed:" + PASSWORD, Role.USER);

        assertRefused("someone", null);
        assertRefused("nobody", null);
    }

    @Test
    void anUnknownUserCannotLogInWithTheDummyPassword() {
        assertRefused("nobody", "dummy");
    }

    /** Without this a response time tells which usernames exist. */
    @Test
    void anUnknownUserCostsTheSameHashingAsAKnownOne() {
        users.add("someone", "hashed:" + PASSWORD, Role.USER);

        assertRefused("someone", "wrongPassword");
        List<String> knownUser = List.copyOf(hasher.calls);
        hasher.calls.clear();
        assertRefused("nobody", "wrongPassword");

        assertThat(hasher.calls).isEqualTo(knownUser).containsExactly("hash", "matches");
    }

    @Test
    void revokesEarlierAccessTokens() {
        users.add("someone", "hashed:" + PASSWORD, Role.USER);
        String first = authenticate.execute("someone", PASSWORD).accessToken();

        String second = authenticate.execute("someone", PASSWORD).accessToken();

        assertThat(tokens.byValue(first).orElseThrow().isActive()).isFalse();
        assertThat(tokens.byValue(second).orElseThrow().isActive()).isTrue();
    }
}
