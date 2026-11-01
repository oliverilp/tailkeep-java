package org.tailkeep.domain.auth;

public record AuthTokens(String accessToken, String refreshToken) {
}
