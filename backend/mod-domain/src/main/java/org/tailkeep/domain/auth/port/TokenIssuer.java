package org.tailkeep.domain.auth.port;

import java.util.Optional;

/** Outbound port that creates signed tokens and reads them back. It knows nothing about stored tokens. */
public interface TokenIssuer {
    String issueAccessToken(String username);

    String issueRefreshToken(String username);

    /** The username in a token this issuer signed and that has not expired. Empty for any other token. */
    Optional<String> usernameIn(String token);
}
