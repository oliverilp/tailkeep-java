package org.tailkeep.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tailkeep.api.dto.PublicConfigDto;

// Public, because the login page needs it. Must never expose secrets.
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigController {

    private final Environment env;

    @GetMapping
    public PublicConfigDto getConfig() {
        boolean isDemo = "true".equalsIgnoreCase(env.getProperty("DEMO_MODE"));
        return new PublicConfigDto(isDemo);
    }
}
