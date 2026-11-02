package org.tailkeep.domain.auth.usecase;

import org.tailkeep.domain.auth.model.AuthTokens;

public interface RefreshToken {
    AuthTokens execute(String refreshToken);
}
