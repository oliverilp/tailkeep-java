package org.tailkeep.domain.auth;

import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.user.FindUser;
import org.tailkeep.domain.user.User;

@UseCase
public class RefreshTokenImpl implements RefreshToken {
    private final FindUser findUser;
    private final TokenIssuer tokenIssuer;
    private final IssueTokens issueTokens;

    public RefreshTokenImpl(FindUser findUser, TokenIssuer tokenIssuer, IssueTokens issueTokens) {
        this.findUser = findUser;
        this.tokenIssuer = tokenIssuer;
        this.issueTokens = issueTokens;
    }

    @Override
    public AuthTokens execute(String refreshToken) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Missing refresh token");
        }
        String username = tokenIssuer.usernameIn(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid token"));
        User user = findUser.byUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return issueTokens.execute(user);
    }
}
