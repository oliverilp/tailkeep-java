package org.tailkeep.domain.user.usecase;

import org.tailkeep.domain.user.port.PasswordHasher;

import java.util.ArrayList;
import java.util.List;

/** Hashes by adding a prefix and records every call, so tests can compare the work two paths do. */
public class FakePasswordHasher implements PasswordHasher {
    public final List<String> calls = new ArrayList<>();

    @Override
    public String hash(String password) {
        calls.add("hash");
        return "hashed:" + password;
    }

    @Override
    public boolean matches(String password, String hash) {
        calls.add("matches");
        return hash.equals("hashed:" + password);
    }
}
