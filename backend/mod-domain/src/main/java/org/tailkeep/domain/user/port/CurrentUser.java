package org.tailkeep.domain.user.port;

import java.util.UUID;

/**
 * Outbound port for who is making the current request. It lets {@code web} pass a user ID to a use case without
 * knowing how {@code security} authenticated the request.
 */
public interface CurrentUser {
    /** Only for routes that require authentication: throws when nobody is logged in. */
    UUID id();
}
