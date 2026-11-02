package org.tailkeep.domain.auth.usecase;

import org.tailkeep.domain.auth.model.AuthTokens;
import org.tailkeep.domain.auth.model.UnauthorizedException;

public interface Authenticate {
    /** Throws {@link UnauthorizedException}, with the same message whether the username or the password is wrong. */
    AuthTokens execute(String username, String password);
}
