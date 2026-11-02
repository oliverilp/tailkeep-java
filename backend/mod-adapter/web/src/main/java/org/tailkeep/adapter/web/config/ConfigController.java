package org.tailkeep.adapter.web.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tailkeep.domain.auth.usecase.GetPublicConfig;

// Public, because the login page needs it. Must never expose secrets.
@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {
    private final GetPublicConfig getPublicConfig;

    public ConfigController(GetPublicConfig getPublicConfig) {
        this.getPublicConfig = getPublicConfig;
    }

    @GetMapping
    public PublicConfigResponse getConfig() {
        return new PublicConfigResponse(getPublicConfig.execute().demoMode());
    }
}
