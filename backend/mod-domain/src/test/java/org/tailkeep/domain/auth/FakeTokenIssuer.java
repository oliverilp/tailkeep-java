package org.tailkeep.domain.auth;

import java.util.Optional;

/** A token is {@code access:<username>:<n>} or {@code refresh:<username>:<n>}. Anything else is not valid. */
class FakeTokenIssuer implements TokenIssuer {
    private int issued;

    @Override
    public String issueAccessToken(String username) {
        return "access:" + username + ":" + ++issued;
    }

    @Override
    public String issueRefreshToken(String username) {
        return "refresh:" + username + ":" + ++issued;
    }

    @Override
    public Optional<String> usernameIn(String token) {
        String[] parts = token.split(":");
        return parts.length == 3 ? Optional.of(parts[1]) : Optional.empty();
    }
}
