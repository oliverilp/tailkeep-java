package org.tailkeep.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.tailkeep.api.config.FakeYtDlpConfig;
import org.tailkeep.api.config.TestContainersConfig;
import org.tailkeep.api.config.TestJwtConfig;
import org.tailkeep.api.support.Api;
import org.tailkeep.api.support.TestDb;
import org.tailkeep.worker.pipeline.FakeYtDlp;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Starts the whole application against real Postgres and Kafka. Subclasses talk to it only through HTTP,
 * the Kafka topics and the database tables, so they don't depend on how the code is organised.
 * <p>
 * The worker's handlers run in this context too, with a fake yt-dlp that is switched off, so they don't answer
 * the messages that tests send to play the worker. A test of the whole pipeline switches it on.
 */
// TestJwtConfig replaces the JwtSecretRepository bean, so overriding is allowed here and only here
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.main.allow-bean-definition-overriding=true")
@Import({
        TestContainersConfig.class,
        TestJwtConfig.class,
        FakeYtDlpConfig.class
})
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
    protected static final String PASSWORD = "password12345";

    @LocalServerPort
    private int port;

    @Autowired
    protected TestDb db;

    @Autowired
    protected FakeYtDlp ytDlp;

    protected TestRestTemplate restTemplate;
    protected Api api;

    @BeforeEach
    void setUp() {
        // Clear any system properties that might affect tests
        System.clearProperty("DEMO_MODE");

        restTemplate = new TestRestTemplate(new RestTemplateBuilder()
                .rootUri("http://localhost:" + port)
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory()));
        api = new Api(restTemplate);

        ytDlp.setAvailable(false);
        db.clean();
    }

    @AfterEach
    void switchOffYtDlp() {
        ytDlp.setAvailable(false);
    }

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    /** Creates a user straight in the table, then logs in over HTTP. Returns the access token. */
    protected String login(String username, String role) {
        String hash = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode(PASSWORD);
        db.insertUser(username, hash, role);
        return authenticate(username, PASSWORD).get("access_token").asText();
    }

    protected String adminToken() {
        return login("admin", "ADMIN");
    }

    protected String userToken() {
        return login("user", "USER");
    }

    protected JsonNode authenticate(String username, String password) {
        Api.Response response = api.post("/api/v1/auth/authenticate",
                "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password), null);
        assertThat(response.status()).isEqualTo(200);
        return response.json();
    }
}
