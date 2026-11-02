package org.tailkeep.domain.auth.usecase;

import org.tailkeep.domain.auth.model.Token;
import org.tailkeep.domain.auth.model.UnauthorizedException;
import org.tailkeep.domain.auth.port.FindToken;
import org.tailkeep.domain.auth.port.TokenIssuer;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.user.model.User;
import org.tailkeep.domain.user.port.FindUser;

import java.util.Optional;

@UseCase
public class IdentifyUserImpl implements IdentifyUser {
    private final FindUser findUser;
    private final FindToken findToken;
    private final TokenIssuer tokenIssuer;

    public IdentifyUserImpl(FindUser findUser, FindToken findToken, TokenIssuer tokenIssuer) {
        this.findUser = findUser;
        this.findToken = findToken;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public Optional<User> execute(String accessToken) {
        String username = tokenIssuer.usernameIn(accessToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid token"));
        User user = findUser.byUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        boolean active = findToken.byValue(accessToken).map(Token::isActive).orElse(false);
        return active ? Optional.of(user) : Optional.empty();
    }
}
