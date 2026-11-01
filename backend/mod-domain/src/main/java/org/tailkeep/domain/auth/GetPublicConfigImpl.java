package org.tailkeep.domain.auth;

import org.tailkeep.domain.shared.UseCase;
import org.tailkeep.domain.user.DeploymentMode;

@UseCase
public class GetPublicConfigImpl implements GetPublicConfig {
    private final DeploymentMode deploymentMode;

    public GetPublicConfigImpl(DeploymentMode deploymentMode) {
        this.deploymentMode = deploymentMode;
    }

    @Override
    public PublicConfig execute() {
        return new PublicConfig(deploymentMode.isDemo());
    }
}
