package org.tailkeep.domain.auth;

public interface Authenticate {
    /** Throws {@link UnauthorizedException}, with the same message whether the username or the password is wrong. */
    AuthTokens execute(String username, String password);
}
