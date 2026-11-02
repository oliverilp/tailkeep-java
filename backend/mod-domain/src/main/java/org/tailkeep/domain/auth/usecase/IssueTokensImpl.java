package org.tailkeep.domain.auth.usecase;

import org.tailkeep.domain.auth.model.AuthTokens;
import org.tailkeep.domain.auth.model.Token;
import org.tailkeep.domain.auth.port.FindToken;
import org.tailkeep.domain.auth.port.SaveToken;
import org.tailkeep.domain.auth.port.TokenIssuer;
import org.tailkeep.domain.shared.RunInTransaction;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.user.model.User;

@UseCase
public class IssueTokensImpl implements IssueTokens {
    private final FindToken findToken;
    private final SaveToken saveToken;
    private final TokenIssuer tokenIssuer;
    private final RunInTransaction runInTransaction;

    public IssueTokensImpl(FindToken findToken, SaveToken saveToken, TokenIssuer tokenIssuer,
            RunInTransaction runInTransaction) {
        this.findToken = findToken;
        this.saveToken = saveToken;
        this.tokenIssuer = tokenIssuer;
        this.runInTransaction = runInTransaction;
    }

    @Override
    public AuthTokens execute(User user) {
        AuthTokens tokens = new AuthTokens(
                tokenIssuer.issueAccessToken(user.username()),
                tokenIssuer.issueRefreshToken(user.username()));

        runInTransaction.execute(() -> {
            saveToken.saveAll(findToken.notRevokedByUser(user.id()).stream().map(Token::revoke).toList());
            saveToken.save(Token.issuedTo(user.id(), tokens.accessToken()));
        });
        return tokens;
    }
}
