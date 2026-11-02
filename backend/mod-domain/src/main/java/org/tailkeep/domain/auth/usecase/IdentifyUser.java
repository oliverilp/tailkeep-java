package org.tailkeep.domain.auth.usecase;

import org.tailkeep.domain.auth.model.UnauthorizedException;
import org.tailkeep.domain.user.model.User;

import java.util.Optional;

/** Finds who a request's access token belongs to. */
public interface IdentifyUser {
    /**
     * Returns the user when the token is stored and still active, and empty when it was revoked. A token that was
     * not signed here, or that names an unknown user, throws {@link UnauthorizedException}.
     */
    Optional<User> execute(String accessToken);
}
