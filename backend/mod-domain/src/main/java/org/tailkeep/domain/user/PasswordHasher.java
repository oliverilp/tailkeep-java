package org.tailkeep.domain.user;

/** Outbound port for one-way password hashing. Both methods are slow on purpose. */
public interface PasswordHasher {
    String hash(String password);

    boolean matches(String password, String hash);
}
