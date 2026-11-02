package org.tailkeep.domain.auth.usecase;

import org.tailkeep.domain.auth.model.AuthTokens;
import org.tailkeep.domain.auth.model.Registration;

public interface Register {
    AuthTokens execute(Registration registration);
}
