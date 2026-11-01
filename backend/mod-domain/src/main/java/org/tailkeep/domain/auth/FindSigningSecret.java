package org.tailkeep.domain.auth;

import java.util.Optional;

public interface FindSigningSecret {
    /** The secret that was saved last. */
    Optional<String> latest();
}
