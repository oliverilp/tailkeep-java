package org.tailkeep.domain.user;

import java.util.Optional;
import java.util.UUID;

public interface FindUser {
    Optional<User> byId(UUID id);

    Optional<User> byUsername(String username);

    long count();
}
