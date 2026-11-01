package org.tailkeep.domain.user;

import java.util.UUID;

public interface ChangePassword {
    void execute(UUID userId, PasswordChange change);
}
