package org.tailkeep.domain.user;

public interface SaveUser {
    /** Inserts the user when it has no id, otherwise updates it. Returns the saved user. */
    User save(User user);
}
