package org.tailkeep.domain.auth;

/** What the login page may know before anyone logs in. Must never hold secrets. */
public record PublicConfig(boolean demoMode) {
}
