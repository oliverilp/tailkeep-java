package org.tailkeep.domain.auth;

import org.tailkeep.domain.user.User;

/**
 * Gives a user new tokens and revokes every access token they had, so one login is valid at a time. Register,
 * Authenticate and RefreshToken end with this.
 */
public interface IssueTokens {
    AuthTokens execute(User user);
}
