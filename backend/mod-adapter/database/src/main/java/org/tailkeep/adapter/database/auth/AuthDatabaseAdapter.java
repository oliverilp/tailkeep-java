package org.tailkeep.adapter.database.auth;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.domain.auth.FindSigningSecret;
import org.tailkeep.domain.auth.FindToken;
import org.tailkeep.domain.auth.SaveSigningSecret;
import org.tailkeep.domain.auth.SaveToken;
import org.tailkeep.domain.auth.Token;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class AuthDatabaseAdapter implements FindToken, SaveToken, FindSigningSecret, SaveSigningSecret {
    private final TokenJpaRepository tokens;
    private final JwtSecretJpaRepository secrets;

    public AuthDatabaseAdapter(TokenJpaRepository tokens, JwtSecretJpaRepository secrets) {
        this.tokens = tokens;
        this.secrets = secrets;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Token> byValue(String value) {
        return tokens.findByToken(value).map(TokenEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Token> notRevokedByUser(UUID userId) {
        return tokens.findNotRevokedByUser(userId).stream().map(TokenEntityMapper::toDomain).toList();
    }

    @Override
    public void save(Token token) {
        tokens.save(TokenEntityMapper.toEntity(token));
    }

    @Override
    public void saveAll(List<Token> tokensToSave) {
        tokens.saveAll(tokensToSave.stream().map(TokenEntityMapper::toEntity).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> latest() {
        return secrets.findLatestSecret().map(JwtSecretEntity::getSecretKey);
    }

    @Override
    public void save(String secret) {
        secrets.save(new JwtSecretEntity(secret));
    }
}
