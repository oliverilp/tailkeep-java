package org.tailkeep.domain.auth;

public interface RefreshToken {
    AuthTokens execute(String refreshToken);
}
