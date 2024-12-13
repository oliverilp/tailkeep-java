package org.tailkeep.api.integration;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.tailkeep.api.config.KafkaMockConfig;
import org.tailkeep.api.config.TestConfig;
import org.tailkeep.api.config.TestContainersConfig;
import org.tailkeep.api.config.TestJwtConfig;
import org.tailkeep.api.dto.AuthenticationResponseDto;
import org.tailkeep.api.dto.RegisterRequestDto;
import org.tailkeep.api.repository.*;
import org.tailkeep.api.service.AuthenticationService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
    TestContainersConfig.class,
    KafkaMockConfig.class,
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

    @Autowired
    protected AuthenticationService authenticationService;

    @PostConstruct
    void init() {
        // Configure RestTemplate to handle authentication errors properly and use the correct root URL
        restTemplate = new TestRestTemplate(new RestTemplateBuilder()
            .rootUri("http://localhost:" + port)
            .errorHandler(new TestRestTemplateResponseErrorHandler()));
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

    protected AuthenticationResponseDto createTestUser(String username, String password) {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .nickname("Test User")
                .username(username)
                .password(password)
                .build();
        return authenticationService.register(request);
    }

    protected TestRestTemplate getRestTemplate() {
        return restTemplate;
    }
} 
