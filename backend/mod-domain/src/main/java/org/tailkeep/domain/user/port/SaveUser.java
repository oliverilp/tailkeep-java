package org.tailkeep.domain.user.port;

import org.tailkeep.domain.user.model.User;

public interface SaveUser {
    /** Inserts the user when it has no id, otherwise updates it. Returns the saved user. */
    User save(User user);
}
