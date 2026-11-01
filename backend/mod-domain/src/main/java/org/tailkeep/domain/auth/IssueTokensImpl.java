package org.tailkeep.domain.auth;

import org.tailkeep.domain.shared.RunInTransaction;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.user.User;

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
