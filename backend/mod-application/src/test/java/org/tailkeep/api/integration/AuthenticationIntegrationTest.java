package org.tailkeep.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.tailkeep.api.support.Api.Response;
import org.tailkeep.api.support.Json;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationIntegrationTest extends BaseIntegrationTest {

    private static String credentials(String username, String password) {
        return "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password);
    }

    private static void assertTokens(JsonNode body) {
        Json.assertKeys(body, "access_token", "refresh_token");
        assertThat(body.get("access_token").asText()).isNotBlank();
        assertThat(body.get("refresh_token").asText()).isNotBlank();
    }

    @Test
    void register_WithValidCredentials_ShouldReturnTokens() {
        Response response = api.post("/api/v1/auth/register",
                "{\"nickname\":\"Test User\",\"username\":\"testuser\",\"password\":\"password12345\"}", null);

        assertThat(response.status()).isEqualTo(200);
        assertTokens(response.json());
        assertThat(db.count("app_user")).isEqualTo(1);
    }

    @Test
    void register_ShouldAlwaysCreateARegularUser() {
        String token = api.post("/api/v1/auth/register",
                "{\"nickname\":\"Test User\",\"username\":\"testuser\",\"password\":\"password12345\"}", null)
                .json().get("access_token").asText();

        // Regular users may not start downloads
        Response response = api.post("/api/v1/downloads", "{\"url\":\"not-a-url\"}", token);

        assertThat(response.status()).isEqualTo(403);
    }

    @Test
    void register_WithExistingUsername_ShouldReturnConflict() {
        login("testuser", "USER");

        Response response = api.post("/api/v1/auth/register",
                "{\"nickname\":\"Other\",\"username\":\"testuser\",\"password\":\"password12345\"}", null);

        assertThat(response.status()).isEqualTo(409);
        Json.assertError(response.body(), 409, "/api/v1/auth/register", "Username already exists");
    }

    @Test
    void register_WithShortPassword_ShouldFail() {
        Response response = api.post("/api/v1/auth/register",
                "{\"nickname\":\"Test User\",\"username\":\"testuser\",\"password\":\"short\"}", null);

        assertThat(response.status()).isEqualTo(400);
        Json.assertError(response.body(), 400, "/api/v1/auth/register",
                "Password must be between 10 and 100 characters long");
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnTokens() {
        login("testuser", "USER");

        Response response = api.post("/api/v1/auth/authenticate", credentials("testuser", PASSWORD), null);

        assertThat(response.status()).isEqualTo(200);
        assertTokens(response.json());
    }

    @Test
    void authenticate_WithWrongPassword_ShouldFail() {
        login("testuser", "USER");

        Response response = api.post("/api/v1/auth/authenticate", credentials("testuser", "wrongpassword"), null);

        assertThat(response.status()).isEqualTo(401);
        Json.assertError(response.body(), 401, "/api/v1/auth/authenticate", "Invalid credentials provided");
    }

    @Test
    void authenticate_WithUnknownUser_ShouldFailTheSameWay() {
        Response response = api.post("/api/v1/auth/authenticate", credentials("nonexistent", "wrongpassword"), null);

        assertThat(response.status()).isEqualTo(401);
        Json.assertError(response.body(), 401, "/api/v1/auth/authenticate", "Invalid credentials provided");
    }

    @Test
    void authenticate_ShouldRevokeEarlierAccessTokens() {
        String first = login("testuser", "USER");
        assertThat(api.get("/api/v1/videos", first).status()).isEqualTo(200);

        authenticate("testuser", PASSWORD);

        assertThat(api.get("/api/v1/videos", first).status()).isEqualTo(401);
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() {
        login("testuser", "USER");
        String refreshToken = authenticate("testuser", PASSWORD).get("refresh_token").asText();

        Response response = api.post("/api/v1/auth/refresh-token", "{\"refreshToken\":\"%s\"}".formatted(refreshToken),
                null);

        assertThat(response.status()).isEqualTo(200);
        assertTokens(response.json());
    }

    @Test
    void refreshToken_WithoutToken_ShouldFail() {
        Response response = api.post("/api/v1/auth/refresh-token", "{}", null);

        assertThat(response.status()).isEqualTo(401);
        Json.assertError(response.body(), 401, "/api/v1/auth/refresh-token", "Missing refresh token");
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldFail() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJhMDljZmE1Yy1iZDEzLTQwZGEtYjg2Yy03Njk0ZWNlZjZhM2YiLCJzdWIiOiJkZW1vIiwiaWF0IjoxNzM0MjA4OTc1LCJleHAiOjE3MzQyOTUzNzV9.SjQ77AIv02SsrFoS8SOs-YXbSfvt6ZTp1MuUsx_wxAQ";

        Response response = api.post("/api/v1/auth/refresh-token", "{\"refreshToken\":\"%s\"}".formatted(token), null);

        assertThat(response.status()).isEqualTo(401);
        Json.assertError(response.body(), 401, "/api/v1/auth/refresh-token", "Invalid token");
    }

    @Test
    void logout_WithValidToken_ShouldRevokeIt() {
        String token = login("testuser", "USER");

        Response response = api.post("/api/v1/auth/logout", null, token);

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body()).isNullOrEmpty();
        assertThat(api.get("/api/v1/videos", token).status()).isEqualTo(401);
    }

    @Test
    void logout_WithoutAuth_ShouldFail() {
        Response response = api.post("/api/v1/auth/logout", null, null);

        assertThat(response.status()).isEqualTo(401);
    }

    @Test
    void logout_WithInvalidToken_ShouldFail() {
        Response response = api.post("/api/v1/auth/logout", null, "invalid-token");

        assertThat(response.status()).isEqualTo(401);
    }

    @Test
    void protectedEndpoint_WithoutToken_ShouldReturnAuthenticationError() {
        Response response = api.get("/api/v1/videos");

        assertThat(response.status()).isEqualTo(401);
        assertThat(response.headers().getContentType().toString()).startsWith("application/json");
        Json.assertError(response.body(), 401, "/api/v1/videos", "Authentication is required to access this resource");
    }
}
