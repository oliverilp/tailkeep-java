package org.tailkeep.domain.auth.usecase;

import org.tailkeep.domain.auth.model.AuthTokens;
import org.tailkeep.domain.auth.model.UnauthorizedException;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.user.model.User;
import org.tailkeep.domain.user.port.FindUser;
import org.tailkeep.domain.user.port.PasswordHasher;

import java.util.Optional;

@UseCase
public class AuthenticateImpl implements Authenticate {
    private static final String INVALID_CREDENTIALS = "Invalid credentials provided";

    private final FindUser findUser;
    private final PasswordHasher passwordHasher;
    private final IssueTokens issueTokens;

    public AuthenticateImpl(FindUser findUser, PasswordHasher passwordHasher, IssueTokens issueTokens) {
        this.findUser = findUser;
        this.passwordHasher = passwordHasher;
        this.issueTokens = issueTokens;
    }

    @Override
    public AuthTokens execute(String username, String password) {
        // Since user registration is not public, we need to prevent username enumeration. Returning immediately
        // would let malicious actors figure out valid usernames from response times, so every attempt does the
        // same work whether the username exists or not: one hash, then one comparison.
        Optional<User> user = findUser.byUsername(username);
        String dummyHash = passwordHasher.hash("dummy");
        String hash = user.map(User::passwordHash).orElse(dummyHash);

        boolean passwordMatches = password != null && passwordHasher.matches(password, hash);
        if (user.isEmpty() || !passwordMatches) {
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }
        return issueTokens.execute(user.get());
    }
}
