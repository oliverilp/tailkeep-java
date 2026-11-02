package org.tailkeep.adapter.security.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.tailkeep.domain.user.port.CurrentUser;

import java.util.UUID;

@Component
public class SecurityContextCurrentUser implements CurrentUser {

    @Override
    public UUID id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser user) {
            return user.id();
        }
        throw new IllegalStateException("Nobody is logged in");
    }
}
