package org.tailkeep.domain.user;

import java.util.Optional;

/** Creates the first user of an empty database. Returns that user, or empty when there already are users. */
public interface SeedInitialUser {
    Optional<User> execute();
}
