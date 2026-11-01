package org.tailkeep.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfig {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:alpine"))
            .withDatabaseName("integration-tests-db")
            .withUsername("test")
            .withPassword("test");

    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
    );

    static {
        postgres.start();
        kafka.start();
        // A @DynamicPropertySource only works on a test class, not on an imported configuration
        System.setProperty("spring.kafka.bootstrap-servers", kafka.getBootstrapServers());
    }

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return postgres;
    }
}
