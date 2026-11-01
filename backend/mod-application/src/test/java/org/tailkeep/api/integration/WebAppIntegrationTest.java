package org.tailkeep.api.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.tailkeep.api.support.Api;
import org.tailkeep.api.support.Json;

import static org.assertj.core.api.Assertions.assertThat;

// Runs against the stub app in src/test/resources/static.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebAppIntegrationTest extends BaseIntegrationTest {

    @ParameterizedTest
    @ValueSource(strings = {"/", "/login", "/dashboard/download", "/dashboard/video/some-id", "/no/such/page"})
    void appRoutes_ShouldReturnIndexWithoutAuthOrCaching(String path) {
        ResponseEntity<String> response = restTemplate.getForEntity(path, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_HTML)).isTrue();
        assertThat(response.getHeaders().getCacheControl()).isEqualTo(CacheControl.noStore().getHeaderValue());
        assertThat(response.getBody()).contains("Test web app");
    }

    @Test
    void indexFile_ShouldNotBeCached() {
        ResponseEntity<String> response = restTemplate.getForEntity("/index.html", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo(CacheControl.noStore().getHeaderValue());
    }

    @Test
    void hashedAssets_ShouldBeCachedForever() {
        ResponseEntity<String> response = restTemplate.getForEntity("/assets/app-abc123.js", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getCacheControl())
            .contains("max-age=31536000")
            .contains("public")
            .contains("immutable");
        assertThat(response.getBody()).contains("test asset");
    }

    @Test
    void publicFiles_ShouldRevalidate() {
        ResponseEntity<String> response = restTemplate.getForEntity("/robots.txt", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo(CacheControl.noCache().getHeaderValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/assets/missing.js", "/missing.png", "/dashboard/missing.js"})
    void missingFiles_ShouldReturn404(String path) {
        ResponseEntity<String> response = restTemplate.getForEntity(path, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).doesNotContain("Test web app");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api", "/api/unknown", "/api/v1/unknown"})
    void unknownApiPaths_WithoutAuth_ShouldReturn401(String path) {
        ResponseEntity<String> response = restTemplate.getForEntity(path, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).doesNotContain("Test web app");
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "PATCH", "DELETE"})
    void unknownApiPaths_WithAuth_ShouldReturn404(String method) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken());

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/unknown",
            HttpMethod.valueOf(method),
            new HttpEntity<>(headers),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).doesNotContain("Test web app");
    }

    @Test
    void writesOutsideTheApi_WithoutAuth_ShouldBeRejected() {
        ResponseEntity<String> response = restTemplate.postForEntity("/login", null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void config_WithoutAuth_ShouldReportDemoModeOff() {
        Api.Response response = api.get("/api/v1/config");

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
        assertThat(Json.assertKeys(response.json(), "demoMode").get("demoMode").isBoolean()).isTrue();
        assertThat(response.json().get("demoMode").asBoolean()).isFalse();
    }

    @Test
    void config_InDemoMode_ShouldReportDemoModeOn() {
        System.setProperty("DEMO_MODE", "true");

        Api.Response response = api.get("/api/v1/config");

        assertThat(response.status()).isEqualTo(200);
        assertThat(Json.assertKeys(response.json(), "demoMode").get("demoMode").asBoolean()).isTrue();
    }
}
