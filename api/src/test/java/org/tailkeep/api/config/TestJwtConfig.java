package org.tailkeep.api.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.tailkeep.api.model.auth.JwtSecret;
import org.tailkeep.api.repository.JwtSecretRepository;

import java.util.Optional;

@TestConfiguration
public class TestJwtConfig {

    private static final String TEST_SECRET = "dGVzdC1qd3Qtc2VjcmV0LWtleS1mb3ItdGVzdGluZy1wdXJwb3Nlcy1vbmx5";

    @Bean
    @Primary
    public JwtSecretRepository jwtSecretRepository() {
        JwtSecretRepository mockRepo = Mockito.mock(JwtSecretRepository.class);

        // Mock the findLatestSecret to always return our test secret
        Mockito.when(mockRepo.findLatestSecret())
                .thenReturn(Optional.of(new JwtSecret(TEST_SECRET)));

        return mockRepo;
    }
} 
