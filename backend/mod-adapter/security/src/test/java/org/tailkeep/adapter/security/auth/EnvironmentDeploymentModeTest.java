package org.tailkeep.adapter.security.auth;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentDeploymentModeTest {
    private final MockEnvironment env = new MockEnvironment();
    private final EnvironmentDeploymentMode mode = new EnvironmentDeploymentMode(env);

    @Test
    void demoModeFollowsTheProperty() {
        assertThat(mode.isDemo()).isFalse();

        env.setProperty("DEMO_MODE", "TRUE");
        assertThat(mode.isDemo()).isTrue();

        env.setProperty("DEMO_MODE", "false");
        assertThat(mode.isDemo()).isFalse();
    }

    @Test
    void productionMeansTheProdProfile() {
        assertThat(mode.isProduction()).isFalse();

        env.setActiveProfiles("dev", "prod");
        assertThat(mode.isProduction()).isTrue();
    }
}
