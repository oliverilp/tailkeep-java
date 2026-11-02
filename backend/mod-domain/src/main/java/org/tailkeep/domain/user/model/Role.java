package org.tailkeep.domain.user.model;

import java.util.Set;

import static org.tailkeep.domain.user.model.Permission.ADMIN_CREATE;
import static org.tailkeep.domain.user.model.Permission.ADMIN_DELETE;
import static org.tailkeep.domain.user.model.Permission.ADMIN_READ;
import static org.tailkeep.domain.user.model.Permission.ADMIN_UPDATE;

public enum Role {
    USER(Set.of()),
    ADMIN(Set.of(ADMIN_READ, ADMIN_UPDATE, ADMIN_DELETE, ADMIN_CREATE));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> permissions() {
        return permissions;
    }
}
