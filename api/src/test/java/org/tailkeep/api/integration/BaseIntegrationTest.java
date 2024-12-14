package org.tailkeep.api.integration;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.tailkeep.api.config.TestConfig;
import org.tailkeep.api.config.TestContainersConfig;
import org.tailkeep.api.config.TestJwtConfig;
import org.tailkeep.api.repository.*;

import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
        TestContainersConfig.class,
        TestConfig.class,
        TestJwtConfig.class
})
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TokenRepository tokenRepository;

    @Autowired
    protected VideoRepository videoRepository;

    @Autowired
    protected ChannelRepository channelRepository;

    @Autowired
    protected JobRepository jobRepository;

    @Autowired
    protected DownloadProgressRepository downloadProgressRepository;

    @PostConstruct
    void init() {
        setupRestTemplate();
    }

    @BeforeEach
    void setUp() {
        cleanupDatabase();
    }

    protected void cleanupDatabase() {
        downloadProgressRepository.deleteAll();
        jobRepository.deleteAll();
        videoRepository.deleteAll();
        channelRepository.deleteAll();
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected TestRestTemplate getRestTemplate() {
        return restTemplate;
    }

    @PostConstruct
    void setupRestTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder()
                .rootUri("http://localhost:" + port)
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .errorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    public boolean hasError(@NotNull ClientHttpResponse response) throws IOException {
                        HttpStatusCode statusCode = response.getStatusCode();
                        return statusCode.is5xxServerError();
                    }
                });

        restTemplate = new TestRestTemplate(builder);
    }
} 
