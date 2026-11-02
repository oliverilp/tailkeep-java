package org.tailkeep.domain.user.usecase;

import org.tailkeep.domain.user.model.PasswordChange;

import java.util.UUID;

public interface ChangePassword {
    void execute(UUID userId, PasswordChange change);
}
