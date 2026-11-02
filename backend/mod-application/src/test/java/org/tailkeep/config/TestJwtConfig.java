package org.tailkeep.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.tailkeep.domain.auth.port.FindSigningSecret;

import java.util.Optional;

@TestConfiguration
public class TestJwtConfig {

    private static final String TEST_SECRET = "dGVzdC1qd3Qtc2VjcmV0LWtleS1mb3ItdGVzdGluZy1wdXJwb3Nlcy1vbmx5";

    @Bean
    @Primary
    public FindSigningSecret testSigningSecret() {
        return () -> Optional.of(TEST_SECRET);
    }
}
