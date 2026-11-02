package org.tailkeep.domain.auth.port;

import org.tailkeep.domain.auth.model.Token;

import java.util.List;

public interface SaveToken {
    /** Inserts the token when it has no id, otherwise updates it. */
    void save(Token token);

    void saveAll(List<Token> tokens);
}
