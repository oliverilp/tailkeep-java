package org.tailkeep.adapter.web.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.tailkeep.domain.auth.model.AuthTokens;

public record AuthenticationResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken) {

    static AuthenticationResponse from(AuthTokens tokens) {
        return new AuthenticationResponse(tokens.accessToken(), tokens.refreshToken());
    }
}
