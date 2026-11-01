package org.tailkeep.adapter.security.auth;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.tailkeep.domain.user.DeploymentMode;

import java.util.Arrays;

@Component
public class EnvironmentDeploymentMode implements DeploymentMode {
    private final Environment env;

    public EnvironmentDeploymentMode(Environment env) {
        this.env = env;
    }

    @Override
    public boolean isDemo() {
        return "true".equalsIgnoreCase(env.getProperty("DEMO_MODE"));
    }

    @Override
    public boolean isProduction() {
        return Arrays.asList(env.getActiveProfiles()).contains("prod");
    }
}
