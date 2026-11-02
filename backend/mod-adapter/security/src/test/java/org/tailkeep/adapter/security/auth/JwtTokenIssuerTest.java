package org.tailkeep.adapter.security.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenIssuerTest {
    private static final String SECRET = "dGVzdC1qd3Qtc2VjcmV0LWtleS1mb3ItdGVzdGluZy1wdXJwb3Nlcy1vbmx5";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

    private static JwtTokenIssuer issuer(String secret) {
        JwtTokenIssuer issuer = new JwtTokenIssuer(() -> Optional.of(secret), s -> {
            throw new AssertionError("must not create a secret");
        });
        issuer.init();
        return issuer;
    }

    private static long lifetimeOf(String token) {
        var claims = Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token).getPayload();
        return claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
    }

    @Test
    void readsTheUsernameBack() {
        JwtTokenIssuer issuer = issuer(SECRET);

        assertThat(issuer.usernameIn(issuer.issueAccessToken("someone"))).contains("someone");
        assertThat(issuer.usernameIn(issuer.issueRefreshToken("someone"))).contains("someone");
    }

    @Test
    void anAccessTokenLastsADayAndARefreshTokenAWeek() {
        JwtTokenIssuer issuer = issuer(SECRET);

        assertThat(lifetimeOf(issuer.issueAccessToken("someone"))).isEqualTo(Duration.ofDays(1).toMillis());
        assertThat(lifetimeOf(issuer.issueRefreshToken("someone"))).isEqualTo(Duration.ofDays(7).toMillis());
    }

    @Test
    void everyTokenIsUnique() {
        JwtTokenIssuer issuer = issuer(SECRET);

        assertThat(issuer.issueAccessToken("someone")).isNotEqualTo(issuer.issueAccessToken("someone"));
    }

    @Test
    void refusesWhatItDidNotSign() {
        String otherSecret = "b3RoZXItand0LXNlY3JldC1rZXktZm9yLXRlc3RpbmctcHVycG9zZXMtb25seQ==";

        assertThat(issuer(SECRET).usernameIn(issuer(otherSecret).issueAccessToken("someone"))).isEmpty();
        assertThat(issuer(SECRET).usernameIn("garbage")).isEmpty();
        assertThat(issuer(SECRET).usernameIn("")).isEmpty();
    }

    @Test
    void refusesAnUnsignedToken() {
        String unsigned = Jwts.builder().subject("someone").expiration(new Date(Long.MAX_VALUE / 2)).compact();

        assertThat(issuer(SECRET).usernameIn(unsigned)).isEmpty();
    }

    @Test
    void refusesAnExpiredToken() {
        String expired = Jwts.builder().subject("someone")
                .expiration(new Date(System.currentTimeMillis() - 60_000)).signWith(KEY).compact();

        assertThat(issuer(SECRET).usernameIn(expired)).isEmpty();
    }

    @Test
    void refusesATokenWithoutAnExpiry() {
        String endless = Jwts.builder().subject("someone").signWith(KEY).compact();

        assertThat(issuer(SECRET).usernameIn(endless)).isEmpty();
    }

    @Test
    void createsAndSavesASecretWhenThereIsNone() {
        List<String> saved = new ArrayList<>();
        JwtTokenIssuer issuer = new JwtTokenIssuer(Optional::empty, saved::add);

        issuer.init();

        assertThat(saved).hasSize(1);
        assertThat(issuer(saved.get(0)).usernameIn(issuer.issueAccessToken("someone"))).contains("someone");
    }
}
