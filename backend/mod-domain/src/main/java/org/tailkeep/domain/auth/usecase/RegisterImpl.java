package org.tailkeep.domain.auth.usecase;

import org.tailkeep.domain.auth.model.AuthTokens;
import org.tailkeep.domain.auth.model.Registration;
import org.tailkeep.domain.shared.RunInTransaction;
import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.user.model.PasswordRules;
import org.tailkeep.domain.user.model.Role;
import org.tailkeep.domain.user.model.User;
import org.tailkeep.domain.user.model.UsernameAlreadyExistsException;
import org.tailkeep.domain.user.port.DeploymentMode;
import org.tailkeep.domain.user.port.FindUser;
import org.tailkeep.domain.user.port.PasswordHasher;
import org.tailkeep.domain.user.port.SaveUser;

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
