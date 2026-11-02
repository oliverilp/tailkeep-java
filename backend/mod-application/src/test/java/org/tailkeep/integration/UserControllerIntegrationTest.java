package org.tailkeep.integration;

import org.junit.jupiter.api.Test;
import org.tailkeep.support.Api.Response;
import org.tailkeep.support.Json;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    private static String change(String current, String next, String confirmation) {
        return "{\"currentPassword\":\"%s\",\"newPassword\":\"%s\",\"confirmationPassword\":\"%s\"}"
                .formatted(current, next, confirmation);
    }

    @Test
    void changePassword_WithoutAuth_ShouldFail() {
        Response response = api.patch("/api/v1/users", change(PASSWORD, "newPassword12345", "newPassword12345"), null);

        assertThat(response.status()).isEqualTo(401);
        Json.assertError(response.body(), 401, "/api/v1/users", "Authentication is required to access this resource");
    }

    @Test
    void changePassword_WithIncorrectCurrentPassword_ShouldFail() {
        String token = userToken();

        Response response = api.patch("/api/v1/users",
                change("wrongPassword12345", "newPassword12345", "newPassword12345"), token);

        assertThat(response.status()).isEqualTo(400);
        Json.assertError(response.body(), 400, "/api/v1/users", "Current password is incorrect");
    }

    @Test
    void changePassword_WithMismatchedNewPasswords_ShouldFail() {
        String token = userToken();

        Response response = api.patch("/api/v1/users", change(PASSWORD, "newPassword12345", "different12345"), token);

        assertThat(response.status()).isEqualTo(400);
        Json.assertError(response.body(), 400, "/api/v1/users", "New passwords do not match");
    }

    @Test
    void changePassword_WithTooShortNewPassword_ShouldFail() {
        String token = userToken();

        Response response = api.patch("/api/v1/users", change(PASSWORD, "short", "short"), token);

        assertThat(response.status()).isEqualTo(400);
        Json.assertError(response.body(), 400, "/api/v1/users",
                "Password must be between 10 and 100 characters long");
    }

    @Test
    void changePassword_WithValidRequest_ShouldReplaceThePassword() {
        String token = userToken();

        Response response = api.patch("/api/v1/users", change(PASSWORD, "newPassword12345", "newPassword12345"), token);

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body()).isNullOrEmpty();
        assertThat(api.post("/api/v1/auth/authenticate",
                "{\"username\":\"user\",\"password\":\"newPassword12345\"}", null).status()).isEqualTo(200);
        assertThat(api.post("/api/v1/auth/authenticate",
                "{\"username\":\"user\",\"password\":\"%s\"}".formatted(PASSWORD), null).status()).isEqualTo(401);
    }

    @Test
    void changePassword_InDemoMode_WithNonAdminUser_ShouldFail() {
        System.setProperty("DEMO_MODE", "true");
        String token = userToken();

        Response response = api.patch("/api/v1/users", change(PASSWORD, "newPassword12345", "newPassword12345"), token);

        assertThat(response.status()).isEqualTo(400);
        Json.assertError(response.body(), 400, "/api/v1/users", "Password change is disabled in demo mode");
    }

    @Test
    void changePassword_InDemoMode_WithAdmin_ShouldSucceed() {
        System.setProperty("DEMO_MODE", "true");
        String token = adminToken();

        Response response = api.patch("/api/v1/users", change(PASSWORD, "newPassword12345", "newPassword12345"), token);

        assertThat(response.status()).isEqualTo(200);
    }
}
