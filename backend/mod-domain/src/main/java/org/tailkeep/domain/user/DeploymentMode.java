package org.tailkeep.domain.user;

/** Outbound port for how this deployment is configured. Read on every call, so never keep the answer. */
public interface DeploymentMode {
    boolean isDemo();

    boolean isProduction();
}
