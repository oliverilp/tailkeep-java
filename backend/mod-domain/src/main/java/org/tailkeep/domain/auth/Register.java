package org.tailkeep.domain.auth;

public interface Register {
    AuthTokens execute(Registration registration);
}
