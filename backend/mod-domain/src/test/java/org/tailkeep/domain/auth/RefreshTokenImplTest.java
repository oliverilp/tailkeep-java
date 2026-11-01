package org.tailkeep.domain.auth;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.download.FakeRunInTransaction;
import org.tailkeep.domain.user.FakeUsers;
import org.tailkeep.domain.user.Role;
import org.tailkeep.domain.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenImplTest {
    private final FakeUsers users = new FakeUsers();
    private final FakeTokens tokens = new FakeTokens();
    private final FakeTokenIssuer issuer = new FakeTokenIssuer();
    private final RefreshToken refreshToken = new RefreshTokenImpl(users, issuer,
            new IssueTokensImpl(tokens, tokens, issuer, new FakeRunInTransaction()));

    private void assertRefused(String token, String message) {
        assertThatThrownBy(() -> refreshToken.execute(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage(message);
    }

    @Test
    void returnsNewTokensAndRevokesEarlierAccessTokens() {
        User user = users.add("someone", "hash", Role.USER);
        tokens.save(Token.issuedTo(user.id(), "access:someone:0"));

        AuthTokens result = refreshToken.execute("refresh:someone:0");

        assertThat(tokens.byValue("access:someone:0").orElseThrow().isActive()).isFalse();
        assertThat(tokens.byValue(result.accessToken()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void refusesAMissingToken() {
        assertRefused(null, "Missing refresh token");
    }

    @Test
    void refusesAnInvalidToken() {
        assertRefused("garbage", "Invalid token");
    }

    @Test
    void refusesATokenOfAnUnknownUser() {
        assertRefused("refresh:nobody:0", "User not found");
    }
}
