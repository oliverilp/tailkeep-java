package org.tailkeep.adapter.security.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.tailkeep.domain.user.model.Role;
import org.tailkeep.domain.user.model.User;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUserTest {

    private static SecurityUser securityUser(Role role) {
        return new SecurityUser(User.builder()
                .id(UUID.randomUUID()).username("someone").passwordHash("hash").role(role).build());
    }

    @Test
    void anAdminHasThePermissionsAndTheRole() {
        assertThat(securityUser(Role.ADMIN).getAuthorities()).map(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("admin:read", "admin:update", "admin:create", "admin:delete", "ROLE_ADMIN");
    }

    @Test
    void aRegularUserOnlyHasTheRole() {
        assertThat(securityUser(Role.USER).getAuthorities()).map(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void anAccountNeverLocksOrExpires() {
        SecurityUser user = securityUser(Role.USER);

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
    }
}
