package org.tailkeep.adapter.security.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.tailkeep.domain.auth.FindSigningSecret;
import org.tailkeep.domain.auth.SaveSigningSecret;
import org.tailkeep.domain.auth.TokenIssuer;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtTokenIssuer implements TokenIssuer {
    private static final long ACCESS_EXPIRATION_MILLIS = 86400000;
    private static final long REFRESH_EXPIRATION_MILLIS = 604800000;

    private final FindSigningSecret findSigningSecret;
    private final SaveSigningSecret saveSigningSecret;
    private SecretKey signingKey;

    public JwtTokenIssuer(FindSigningSecret findSigningSecret, SaveSigningSecret saveSigningSecret) {
        this.findSigningSecret = findSigningSecret;
        this.saveSigningSecret = saveSigningSecret;
    }

    @PostConstruct
    public void init() {
        String secret = findSigningSecret.latest().orElseGet(this::generateAndSaveNewSecret);
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    private String generateAndSaveNewSecret() {
        SecretKey key = Jwts.SIG.HS256.key().build();
        String secret = Base64.getEncoder().encodeToString(key.getEncoded());
        saveSigningSecret.save(secret);
        return secret;
    }

    @Override
    public String issueAccessToken(String username) {
        return buildToken(username, ACCESS_EXPIRATION_MILLIS);
    }

    @Override
    public String issueRefreshToken(String username) {
        return buildToken(username, REFRESH_EXPIRATION_MILLIS);
    }

    private String buildToken(String username, long expirationMillis) {
        long now = System.currentTimeMillis();
        return Jwts
                .builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMillis))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public Optional<String> usernameIn(String token) {
        try {
            // The parser rejects an unsigned, wrongly signed or expired token, but accepts one without an expiry
            Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
            return claims.getExpiration() == null ? Optional.empty() : Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
