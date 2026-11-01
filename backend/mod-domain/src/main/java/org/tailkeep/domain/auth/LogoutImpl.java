package org.tailkeep.domain.auth;

import org.tailkeep.domain.shared.RunInTransaction;
import org.tailkeep.domain.shared.UseCase;

import java.util.Optional;

@UseCase
public class LogoutImpl implements Logout {
    private final FindToken findToken;
    private final SaveToken saveToken;
    private final TokenIssuer tokenIssuer;
    private final RunInTransaction runInTransaction;

    public LogoutImpl(FindToken findToken, SaveToken saveToken, TokenIssuer tokenIssuer,
            RunInTransaction runInTransaction) {
        this.findToken = findToken;
        this.saveToken = saveToken;
        this.tokenIssuer = tokenIssuer;
        this.runInTransaction = runInTransaction;
    }

    @Override
    public boolean execute(String accessToken) {
        if (tokenIssuer.usernameIn(accessToken).isEmpty()) {
            return false;
        }
        return runInTransaction.execute(() -> {
            Optional<Token> token = findToken.byValue(accessToken).filter(Token::isActive);
            token.ifPresent(active -> saveToken.save(active.revoke()));
            return token.isPresent();
        });
    }
}
