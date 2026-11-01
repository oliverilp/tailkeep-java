package org.tailkeep.domain.auth;

import java.util.List;

public interface SaveToken {
    /** Inserts the token when it has no id, otherwise updates it. */
    void save(Token token);

    void saveAll(List<Token> tokens);
}
