package org.tailkeep.domain.user;

import org.tailkeep.domain.shared.ResourceNotFoundException;
import org.tailkeep.domain.shared.UseCase;

import java.util.UUID;

@UseCase
public class ChangePasswordImpl implements ChangePassword {
    private final FindUser findUser;
    private final SaveUser saveUser;
    private final PasswordHasher passwordHasher;
    private final DeploymentMode deploymentMode;

    public ChangePasswordImpl(FindUser findUser, SaveUser saveUser, PasswordHasher passwordHasher,
            DeploymentMode deploymentMode) {
        this.findUser = findUser;
        this.saveUser = saveUser;
        this.passwordHasher = passwordHasher;
        this.deploymentMode = deploymentMode;
    }

    @Override
    public void execute(UUID userId, PasswordChange change) {
        User user = findUser.byId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        if (!user.mayChangePassword(deploymentMode.isDemo())) {
            throw new UnsupportedOperationException("Password change is disabled in demo mode");
        }

        PasswordRules.requireValidLength(change.newPassword());
        PasswordRules.requireValidLength(change.confirmationPassword());

        if (!passwordHasher.matches(change.currentPassword(), user.passwordHash())) {
            throw new InvalidCurrentPasswordException("Current password is incorrect");
        }
        if (!change.newPassword().equals(change.confirmationPassword())) {
            throw new PasswordMismatchException("New passwords do not match");
        }

        saveUser.save(user.withPasswordHash(passwordHasher.hash(change.newPassword())));
    }
}
