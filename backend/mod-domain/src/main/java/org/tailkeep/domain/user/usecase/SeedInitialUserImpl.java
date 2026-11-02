package org.tailkeep.domain.user.usecase;

import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.user.model.Role;
import org.tailkeep.domain.user.model.User;
import org.tailkeep.domain.user.port.DeploymentMode;
import org.tailkeep.domain.user.port.FindUser;
import org.tailkeep.domain.user.port.PasswordHasher;
import org.tailkeep.domain.user.port.SaveUser;

import java.util.Optional;

@UseCase
public class SeedInitialUserImpl implements SeedInitialUser {
    private static final String ADMIN_NICKNAME = "Admin";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin1Admin1";

    private static final String DEMO_NICKNAME = "Demo";
    private static final String DEMO_USERNAME = "demo";
    private static final String DEMO_PASSWORD = "Demo1Demo1";

    private final FindUser findUser;
    private final SaveUser saveUser;
    private final PasswordHasher passwordHasher;
    private final DeploymentMode deploymentMode;

    public SeedInitialUserImpl(FindUser findUser, SaveUser saveUser, PasswordHasher passwordHasher,
            DeploymentMode deploymentMode) {
        this.findUser = findUser;
        this.saveUser = saveUser;
        this.passwordHasher = passwordHasher;
        this.deploymentMode = deploymentMode;
    }

    @Override
    public Optional<User> execute() {
        if (findUser.count() != 0) {
            return Optional.empty();
        }
        if (deploymentMode.isDemo()) {
            return Optional.of(seed(DEMO_NICKNAME, DEMO_USERNAME, DEMO_PASSWORD, Role.USER));
        }
        return Optional.of(seed(ADMIN_NICKNAME, ADMIN_USERNAME, ADMIN_PASSWORD, Role.ADMIN));
    }

    private User seed(String nickname, String username, String password, Role role) {
        return saveUser.save(User.builder()
                .nickname(nickname)
                .username(username)
                .passwordHash(passwordHasher.hash(password))
                .role(role)
                .build());
    }
}
