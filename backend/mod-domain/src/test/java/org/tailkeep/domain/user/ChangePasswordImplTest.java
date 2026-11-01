package org.tailkeep.domain.user;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.shared.ResourceNotFoundException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangePasswordImplTest {
    private static final String CURRENT = "currentPassword";
    private static final String NEW = "newPassword1";

    private final FakeUsers users = new FakeUsers();
    private final FakeDeploymentMode mode = new FakeDeploymentMode();
    private final ChangePassword changePassword =
            new ChangePasswordImpl(users, users, new FakePasswordHasher(), mode);

    private User user(Role role) {
        return users.add("someone", "hashed:" + CURRENT, role);
    }

    private String hashOf(User user) {
        return users.byId(user.id()).orElseThrow().passwordHash();
    }

    @Test
    void replacesThePasswordHash() {
        User user = user(Role.USER);

        changePassword.execute(user.id(), new PasswordChange(CURRENT, NEW, NEW));

        assertThat(hashOf(user)).isEqualTo("hashed:" + NEW);
    }

    @Test
    void refusesAWrongCurrentPassword() {
        User user = user(Role.USER);

        assertThatThrownBy(() -> changePassword.execute(user.id(), new PasswordChange("wrongPassword", NEW, NEW)))
                .isInstanceOf(InvalidCurrentPasswordException.class)
                .hasMessage("Current password is incorrect");
        assertThat(hashOf(user)).isEqualTo("hashed:" + CURRENT);
    }

    @Test
    void refusesDifferentNewPasswords() {
        User user = user(Role.USER);

        assertThatThrownBy(() -> changePassword.execute(user.id(), new PasswordChange(CURRENT, NEW, "otherPassword")))
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessage("New passwords do not match");
    }

    @Test
    void checksTheLengthBeforeTheCurrentPassword() {
        User user = user(Role.USER);

        assertThatThrownBy(() -> changePassword.execute(user.id(), new PasswordChange("wrongPassword", "short", "short")))
                .isInstanceOf(PasswordLengthException.class);
    }

    @Test
    void refusesARegularUserInDemoMode() {
        mode.demo = true;
        User user = user(Role.USER);

        assertThatThrownBy(() -> changePassword.execute(user.id(), new PasswordChange(CURRENT, NEW, NEW)))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Password change is disabled in demo mode");
        assertThat(hashOf(user)).isEqualTo("hashed:" + CURRENT);
    }

    @Test
    void allowsAnAdminInDemoMode() {
        mode.demo = true;
        User user = user(Role.ADMIN);

        changePassword.execute(user.id(), new PasswordChange(CURRENT, NEW, NEW));

        assertThat(hashOf(user)).isEqualTo("hashed:" + NEW);
    }

    @Test
    void anUnknownUserIsNotFound() {
        assertThatThrownBy(() -> changePassword.execute(UUID.randomUUID(), new PasswordChange(CURRENT, NEW, NEW)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
