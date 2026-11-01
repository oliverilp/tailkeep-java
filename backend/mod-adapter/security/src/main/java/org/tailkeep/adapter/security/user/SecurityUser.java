package org.tailkeep.adapter.security.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.tailkeep.domain.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A domain {@link User} as Spring Security sees it. Accounts never lock or expire. */
public class SecurityUser implements UserDetails {
    private final UUID id;
    private final String username;
    private final String passwordHash;
    private final List<GrantedAuthority> authorities;

    public SecurityUser(User user) {
        this.id = user.id();
        this.username = user.username();
        this.passwordHash = user.passwordHash();
        this.authorities = authoritiesOf(user);
    }

    private static List<GrantedAuthority> authoritiesOf(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        user.role().permissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.value())));
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.role().name()));
        return authorities;
    }

    public UUID id() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
