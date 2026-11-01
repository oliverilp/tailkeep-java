package org.tailkeep.domain.auth;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindToken {
    Optional<Token> byValue(String value);

    /** The user's tokens that are not both expired and revoked. */
    List<Token> notRevokedByUser(UUID userId);
}
