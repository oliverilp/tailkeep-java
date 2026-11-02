package org.tailkeep.domain.user.model;

public enum Permission {
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete");

    private final String value;

    Permission(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
