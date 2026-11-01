package org.tailkeep.domain.auth;

public interface Logout {
    /** Revokes the access token. Returns false when it is not one that can still be used. */
    boolean execute(String accessToken);
}
