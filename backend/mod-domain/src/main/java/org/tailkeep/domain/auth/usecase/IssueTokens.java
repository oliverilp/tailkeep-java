package org.tailkeep.domain.auth.usecase;

import org.tailkeep.domain.auth.model.AuthTokens;
import org.tailkeep.domain.user.model.User;

/**
 * Gives a user new tokens and revokes every access token they had, so one login is valid at a time. Register,
 * Authenticate and RefreshToken end with this.
 */
public interface IssueTokens {
    AuthTokens execute(User user);
}
