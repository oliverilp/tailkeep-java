package org.tailkeep.domain.user.usecase;

import org.tailkeep.domain.user.port.DeploymentMode;

public class FakeDeploymentMode implements DeploymentMode {
    public boolean demo;
    public boolean production;

    @Override
    public boolean isDemo() {
        return demo;
    }

    @Override
    public boolean isProduction() {
        return production;
    }
}
