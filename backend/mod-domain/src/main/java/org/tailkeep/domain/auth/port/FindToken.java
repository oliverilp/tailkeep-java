package org.tailkeep.domain.auth.port;

import org.tailkeep.domain.auth.model.Token;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindToken {
    Optional<Token> byValue(String value);

    /** The user's tokens that are not both expired and revoked. */
    List<Token> notRevokedByUser(UUID userId);
}
