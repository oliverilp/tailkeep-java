package org.tailkeep.domain.user;

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
