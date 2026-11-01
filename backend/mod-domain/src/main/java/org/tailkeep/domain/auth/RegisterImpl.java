package org.tailkeep.domain.auth;

import org.tailkeep.domain.shared.RunInTransaction;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.user.DeploymentMode;
import org.tailkeep.domain.user.FindUser;
import org.tailkeep.domain.user.PasswordHasher;
import org.tailkeep.domain.user.PasswordRules;
import org.tailkeep.domain.user.Role;
import org.tailkeep.domain.user.SaveUser;
import org.tailkeep.domain.user.User;
import org.tailkeep.domain.user.UsernameAlreadyExistsException;

@UseCase
public class RegisterImpl implements Register {
    private final FindUser findUser;
    private final SaveUser saveUser;
    private final PasswordHasher passwordHasher;
    private final IssueTokens issueTokens;
    private final DeploymentMode deploymentMode;
    private final RunInTransaction runInTransaction;

    public RegisterImpl(FindUser findUser, SaveUser saveUser, PasswordHasher passwordHasher, IssueTokens issueTokens,
            DeploymentMode deploymentMode, RunInTransaction runInTransaction) {
        this.findUser = findUser;
        this.saveUser = saveUser;
        this.passwordHasher = passwordHasher;
        this.issueTokens = issueTokens;
        this.deploymentMode = deploymentMode;
        this.runInTransaction = runInTransaction;
    }

    @Override
    public AuthTokens execute(Registration registration) {
        if (deploymentMode.isProduction()) {
            throw new UnsupportedOperationException("Registration is disabled in production");
        }
        PasswordRules.requireValidLength(registration.password());

        return runInTransaction.execute(() -> {
            if (findUser.byUsername(registration.username()).isPresent()) {
                throw new UsernameAlreadyExistsException("Username already exists");
            }
            // Always a regular user: an admin is only ever seeded
            User user = saveUser.save(User.builder()
                    .nickname(registration.nickname())
                    .username(registration.username())
                    .passwordHash(passwordHasher.hash(registration.password()))
                    .role(Role.USER)
                    .build());
            return issueTokens.execute(user);
        });
    }
}
