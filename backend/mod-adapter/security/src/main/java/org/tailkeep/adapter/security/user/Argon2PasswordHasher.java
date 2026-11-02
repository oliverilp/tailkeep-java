package org.tailkeep.adapter.security.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.tailkeep.domain.user.port.PasswordHasher;

@Component
public class Argon2PasswordHasher implements PasswordHasher {
    private final PasswordEncoder passwordEncoder;

    public Argon2PasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean matches(String password, String hash) {
        return passwordEncoder.matches(password, hash);
    }
}
