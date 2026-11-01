package org.tailkeep.domain.auth;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.user.FakeUsers;
import org.tailkeep.domain.user.Role;
import org.tailkeep.domain.user.User;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentifyUserImplTest {
    private static final String TOKEN = "access:someone:0";

    private final FakeUsers users = new FakeUsers();
    private final FakeTokens tokens = new FakeTokens();
    private final IdentifyUser identifyUser = new IdentifyUserImpl(users, tokens, new FakeTokenIssuer());

    @Test
    void findsTheUserOfAnActiveToken() {
        User user = users.add("someone", "hash", Role.USER);
        tokens.save(Token.issuedTo(user.id(), TOKEN));

        assertThat(identifyUser.execute(TOKEN)).contains(user);
    }

    @Test
    void aRevokedTokenIdentifiesNobody() {
        User user = users.add("someone", "hash", Role.USER);
        tokens.save(Token.issuedTo(user.id(), TOKEN).revoke());

        assertThat(identifyUser.execute(TOKEN)).isEmpty();
    }

    @Test
    void aSignedTokenThatIsNotStoredIdentifiesNobody() {
        users.add("someone", "hash", Role.USER);

        assertThat(identifyUser.execute("refresh:someone:0")).isEmpty();
    }

    @Test
    void refusesAnInvalidToken() {
        assertThatThrownBy(() -> identifyUser.execute("garbage")).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refusesATokenOfAnUnknownUser() {
        tokens.save(Token.issuedTo(UUID.randomUUID(), TOKEN));

        assertThatThrownBy(() -> identifyUser.execute(TOKEN)).isInstanceOf(UnauthorizedException.class);
    }
}
