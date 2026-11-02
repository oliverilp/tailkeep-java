package org.tailkeep.domain.auth.usecase;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.auth.model.AuthTokens;
import org.tailkeep.domain.auth.model.Registration;
import org.tailkeep.domain.download.usecase.FakeRunInTransaction;
import org.tailkeep.domain.user.model.PasswordLengthException;
import org.tailkeep.domain.user.model.Role;
import org.tailkeep.domain.user.model.User;
import org.tailkeep.domain.user.model.UsernameAlreadyExistsException;
import org.tailkeep.domain.user.usecase.FakeDeploymentMode;
import org.tailkeep.domain.user.usecase.FakePasswordHasher;
import org.tailkeep.domain.user.usecase.FakeUsers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterImplTest {
    private static final Registration REGISTRATION = new Registration("Nick", "someone", "password12345");

    private final FakeUsers users = new FakeUsers();
    private final FakeTokens tokens = new FakeTokens();
    private final FakeDeploymentMode mode = new FakeDeploymentMode();
    private final FakeRunInTransaction transaction = new FakeRunInTransaction();
    private final Register register = new RegisterImpl(users, users, new FakePasswordHasher(),
            new IssueTokensImpl(tokens, tokens, new FakeTokenIssuer(), transaction), mode, transaction);

    @Test
    void createsARegularUserWithAHashedPassword() {
        AuthTokens result = register.execute(REGISTRATION);

        User user = users.byUsername("someone").orElseThrow();
        assertThat(user.nickname()).isEqualTo("Nick");
        assertThat(user.passwordHash()).isEqualTo("hashed:password12345");
        assertThat(user.role()).isEqualTo(Role.USER);
        assertThat(tokens.byValue(result.accessToken()).orElseThrow().userId()).isEqualTo(user.id());
    }

    @Test
    void refusesAnExistingUsername() {
        users.add("someone", "hash", Role.USER);

        assertThatThrownBy(() -> register.execute(REGISTRATION))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void refusesABadPasswordLength() {
        assertThatThrownBy(() -> register.execute(new Registration("Nick", "someone", "short")))
                .isInstanceOf(PasswordLengthException.class);
        assertThat(users.saved).isEmpty();
    }

    @Test
    void isDisabledInProduction() {
        mode.production = true;

        assertThatThrownBy(() -> register.execute(REGISTRATION))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Registration is disabled in production");
        assertThat(users.saved).isEmpty();
    }
}
