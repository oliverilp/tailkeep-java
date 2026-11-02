package org.tailkeep.domain.user.usecase;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.user.model.Role;
import org.tailkeep.domain.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class SeedInitialUserImplTest {
    private final FakeUsers users = new FakeUsers();
    private final FakeDeploymentMode mode = new FakeDeploymentMode();
    private final SeedInitialUser seed = new SeedInitialUserImpl(users, users, new FakePasswordHasher(), mode);

    @Test
    void seedsAnAdmin() {
        User user = seed.execute().orElseThrow();

        assertThat(user.nickname()).isEqualTo("Admin");
        assertThat(user.username()).isEqualTo("admin");
        assertThat(user.passwordHash()).isEqualTo("hashed:Admin1Admin1");
        assertThat(user.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void seedsARegularDemoUserInDemoMode() {
        mode.demo = true;

        User user = seed.execute().orElseThrow();

        assertThat(user.nickname()).isEqualTo("Demo");
        assertThat(user.username()).isEqualTo("demo");
        assertThat(user.passwordHash()).isEqualTo("hashed:Demo1Demo1");
        assertThat(user.role()).isEqualTo(Role.USER);
    }

    @Test
    void doesNothingWhenThereAreUsers() {
        users.add("someone", "hash", Role.USER);

        assertThat(seed.execute()).isEmpty();
        assertThat(users.saved).hasSize(1);
    }
}
