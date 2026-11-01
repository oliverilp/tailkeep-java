package org.tailkeep.domain.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void anAdminHasEveryPermission() {
        assertThat(Role.ADMIN.permissions()).containsExactlyInAnyOrder(Permission.values());
    }

    @Test
    void aRegularUserHasNoPermissions() {
        assertThat(Role.USER.permissions()).isEmpty();
    }

    @Test
    void permissionsHaveTheirAuthorityNames() {
        assertThat(Permission.ADMIN_READ.value()).isEqualTo("admin:read");
        assertThat(Permission.ADMIN_UPDATE.value()).isEqualTo("admin:update");
        assertThat(Permission.ADMIN_CREATE.value()).isEqualTo("admin:create");
        assertThat(Permission.ADMIN_DELETE.value()).isEqualTo("admin:delete");
    }
}
