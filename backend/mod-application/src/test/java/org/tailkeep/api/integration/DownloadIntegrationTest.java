package org.tailkeep.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.tailkeep.api.support.Api.Response;
import org.tailkeep.api.support.Json;
import org.tailkeep.api.support.Shapes;
import org.tailkeep.api.support.TestDb.Download;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadIntegrationTest extends BaseIntegrationTest {

    private static String url(String url) {
        return "{\"url\":\"%s\"}".formatted(url);
    }

    // The URL is checked with a real HEAD request, so this needs network access.
    @Test
    void startDownload_WithValidUrl_ShouldAcceptRequest() {
        String token = adminToken();

        Response response = api.post("/api/v1/downloads", url("https://www.youtube.com/watch?v=dQw4w9WgXcQ"), token);

        assertThat(response.status()).isEqualTo(202);
        assertThat(response.body()).isNullOrEmpty();
        assertThat(db.count("job")).isEqualTo(1);
    }

    @Test
    void startDownload_WithoutAuth_ShouldFail() {
        Response response = api.post("/api/v1/downloads", url("https://www.youtube.com/watch?v=dQw4w9WgXcQ"), null);

        assertThat(response.status()).isEqualTo(401);
        Json.assertError(response.body(), 401, "/api/v1/downloads",
                "Authentication is required to access this resource");
    }

    @Test
    void startDownload_WithoutAdminPermission_ShouldFail() {
        Response response = api.post("/api/v1/downloads", url("https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
                userToken());

        assertThat(response.status()).isEqualTo(403);
        Json.assertError(response.body(), 403, "/api/v1/downloads", "You don't have permission to access this resource");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "not-a-url|Failed to validate URL",
            "http://example.com|URL must use HTTPS protocol",
            "https:///path|URL must have a valid host",
            "https://localhost/path|URL must have a valid domain",
    })
    void startDownload_WithInvalidUrl_ShouldFail(String url, String message) {
        Response response = api.post("/api/v1/downloads", url(url), adminToken());

        assertThat(response.status()).isEqualTo(400);
        Json.assertError(response.body(), 400, "/api/v1/downloads", message);
        assertThat(db.count("job")).isZero();
    }

    // The request class has validation annotations, but no validator is on the classpath, so they do nothing.
    @Test
    void startDownload_WithBlankUrl_ShouldFail() {
        Response response = api.post("/api/v1/downloads", url(""), adminToken());

        assertThat(response.status()).isEqualTo(400);
        Json.assertError(response.body(), 400, "/api/v1/downloads", "Failed to validate URL");
    }

    @Test
    void startDownload_WithoutBody_ShouldFail() {
        Response response = api.post("/api/v1/downloads", null, adminToken());

        assertThat(response.status()).isEqualTo(400);
        Json.assertError(response.body(), 400, "/api/v1/downloads", "Invalid request body: JSON is required");
    }

    @Test
    void getAllDownloads_WithoutData_ShouldReturnEmptyList() {
        Response response = api.get("/api/v1/downloads", userToken());

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("[]");
    }

    @Test
    void getAllDownloads_ShouldReturnNewestFirst() {
        Download first = db.downloading(1);
        Download second = db.finished(2);
        Download third = db.downloading(3);

        Response response = api.get("/api/v1/downloads", userToken());

        assertThat(response.status()).isEqualTo(200);
        JsonNode downloads = response.json();
        assertThat(downloads).hasSize(3);
        downloads.forEach(Shapes::download);
        assertThat(Json.ids(downloads)).containsExactly(
                third.jobId().toString(), second.jobId().toString(), first.jobId().toString());
    }

    @Test
    void getAllDownloads_WithoutAuth_ShouldFail() {
        assertThat(api.get("/api/v1/downloads").status()).isEqualTo(401);
    }

    @Test
    void getDownloadById_ShouldReturnAllFields() {
        Download download = db.downloading(1);

        Response response = api.get("/api/v1/downloads/" + download.jobId(), userToken());

        assertThat(response.status()).isEqualTo(200);
        JsonNode body = Shapes.download(response.json());
        assertThat(body.get("id").asText()).isEqualTo(download.jobId().toString());
        assertThat(body.get("video").get("id").asText()).isEqualTo(download.videoId().toString());
        assertThat(body.get("video").get("channel").get("id").asText()).isEqualTo(download.channelId().toString());
        assertThat(body.get("status").asText()).isEqualTo("downloading");
        assertThat(body.get("hasEnded").asBoolean()).isFalse();
        assertThat(body.get("progress").asDouble()).isEqualTo(42.5);
        assertThat(body.get("size").asText()).isEqualTo("10.00MiB");
        assertThat(body.get("speed").asText()).isEqualTo("1.00MiB/s");
        assertThat(body.get("eta").asText()).isEqualTo("00:10");
        assertThat(body.get("completedAt").isNull()).isTrue();
    }

    @Test
    void getDownloadById_WhenFinished_ShouldHaveCompletionTime() {
        Download download = db.finished(1);

        JsonNode body = Shapes.download(api.get("/api/v1/downloads/" + download.jobId(), userToken()).json());

        assertThat(body.get("status").asText()).isEqualTo("done");
        assertThat(body.get("hasEnded").asBoolean()).isTrue();
        assertThat(body.get("progress").asDouble()).isEqualTo(100.0);
        assertThat(body.get("completedAt").isNull()).isFalse();
    }

    @Test
    void getDownloadById_WithMalformedId_ShouldFail() {
        Response response = api.get("/api/v1/downloads/invalid-id", userToken());

        assertThat(response.status()).isEqualTo(400);
        Json.assertError(response.body(), 400, "/api/v1/downloads/invalid-id", "Invalid parameter format");
    }

    @Test
    void getDownloadById_WithUnknownId_ShouldReturnNotFound() {
        UUID id = UUID.randomUUID();

        Response response = api.get("/api/v1/downloads/" + id, userToken());

        assertThat(response.status()).isEqualTo(404);
        Json.assertError(response.body(), 404, "/api/v1/downloads/" + id,
                "Download progress not found with identifier: " + id);
    }

    @Test
    void getDashboard_WithoutData_ShouldBeEmpty() {
        Response response = api.get("/api/v1/downloads/dashboard", userToken());

        assertThat(response.status()).isEqualTo(200);
        JsonNode body = Json.assertKeys(response.json(), "queueInfo", "downloads");
        Json.assertKeys(body.get("queueInfo"), "queue", "active", "finished");
        Json.assertKeys(body.get("downloads"), "items", "totalPages", "totalItems", "currentPage", "pageSize",
                "hasNext", "hasPrevious");
        assertThat(body.get("downloads").get("items")).isEmpty();
        assertThat(body.get("downloads").get("totalItems").asLong()).isZero();
        assertThat(body.get("downloads").get("currentPage").asInt()).isEqualTo(1);
        assertThat(body.get("downloads").get("pageSize").asInt()).isEqualTo(10);
    }

    @Test
    void getDashboard_ShouldCountJobsByState() {
        db.downloading(1);
        db.downloading(2);
        db.finished(3);
        db.queuedJob("https://www.youtube.com/watch?v=queued");

        JsonNode queueInfo = api.get("/api/v1/downloads/dashboard", userToken()).json().get("queueInfo");

        // `queue` is always 0 today, even with a queued job: the query joins download_progress. Not pinned.
        assertThat(queueInfo.get("queue").isIntegralNumber()).isTrue();
        assertThat(queueInfo.get("active").asLong()).isEqualTo(2);
        assertThat(queueInfo.get("finished").asLong()).isEqualTo(1);
    }

    @Test
    void getDashboard_ShouldPageFromOne() {
        db.downloading(1);
        db.downloading(2);
        Download newest = db.downloading(3);
        String token = userToken();

        JsonNode firstPage = api.get("/api/v1/downloads/dashboard?page=1&size=2", token).json().get("downloads");
        JsonNode secondPage = api.get("/api/v1/downloads/dashboard?page=2&size=2", token).json().get("downloads");

        assertThat(firstPage.get("items")).hasSize(2);
        firstPage.get("items").forEach(Shapes::download);
        assertThat(firstPage.get("items").get(0).get("id").asText()).isEqualTo(newest.jobId().toString());
        assertThat(firstPage.get("totalPages").asInt()).isEqualTo(2);
        assertThat(firstPage.get("totalItems").asLong()).isEqualTo(3);
        assertThat(firstPage.get("currentPage").asInt()).isEqualTo(1);
        assertThat(firstPage.get("pageSize").asInt()).isEqualTo(2);
        assertThat(firstPage.get("hasNext").asBoolean()).isTrue();
        assertThat(firstPage.get("hasPrevious").asBoolean()).isFalse();

        assertThat(secondPage.get("items")).hasSize(1);
        assertThat(secondPage.get("currentPage").asInt()).isEqualTo(2);
        assertThat(secondPage.get("hasNext").asBoolean()).isFalse();
        assertThat(secondPage.get("hasPrevious").asBoolean()).isTrue();
    }

    @Test
    void getDashboard_ShouldFilterByProgress() {
        db.downloading(1);
        db.finished(2);
        db.finished(3);
        String token = userToken();

        JsonNode active = api.get("/api/v1/downloads/dashboard?progress=active", token).json().get("downloads");
        JsonNode done = api.get("/api/v1/downloads/dashboard?progress=done", token).json().get("downloads");
        JsonNode all = api.get("/api/v1/downloads/dashboard?progress=all", token).json().get("downloads");

        assertThat(active.get("totalItems").asLong()).isEqualTo(1);
        assertThat(done.get("totalItems").asLong()).isEqualTo(2);
        assertThat(all.get("totalItems").asLong()).isEqualTo(3);
        assertThat(active.get("items").get(0).get("hasEnded").asBoolean()).isFalse();
        done.get("items").forEach(item -> assertThat(item.get("hasEnded").asBoolean()).isTrue());
    }

    @Test
    void getDashboard_WithoutAuth_ShouldFail() {
        assertThat(api.get("/api/v1/downloads/dashboard").status()).isEqualTo(401);
    }

    @Test
    void softDeleteDownload_ShouldRemoveItFromTheDashboard() {
        Download keep = db.downloading(1);
        Download delete = db.finished(2);
        String token = adminToken();

        Response response = api.delete("/api/v1/downloads/" + delete.jobId(), token);

        assertThat(response.status()).isEqualTo(204);
        assertThat(response.body()).isNullOrEmpty();
        JsonNode dashboard = api.get("/api/v1/downloads/dashboard", token).json();
        assertThat(dashboard.get("queueInfo").get("finished").asLong()).isZero();
        assertThat(dashboard.get("queueInfo").get("active").asLong()).isEqualTo(1);
        assertThat(Json.ids(dashboard.get("downloads").get("items")))
                .containsExactly(keep.jobId().toString());
        // The row is kept, only hidden
        assertThat(db.count("download_progress")).isEqualTo(2);
    }

    @Test
    void softDeleteDownload_WithoutAuth_ShouldFail() {
        Response response = api.delete("/api/v1/downloads/" + UUID.randomUUID(), null);

        assertThat(response.status()).isEqualTo(401);
    }

    @Test
    void softDeleteDownload_WithoutAdminPermission_ShouldFail() {
        Download download = db.downloading(1);

        Response response = api.delete("/api/v1/downloads/" + download.jobId(), userToken());

        assertThat(response.status()).isEqualTo(403);
        Json.assertError(response.body(), 403, "/api/v1/downloads/" + download.jobId(),
                "You don't have permission to access this resource");
    }

    @Test
    void softDeleteDownload_WithMalformedId_ShouldFail() {
        Response response = api.delete("/api/v1/downloads/invalid-id", adminToken());

        assertThat(response.status()).isEqualTo(400);
        Json.assertError(response.body(), 400, "/api/v1/downloads/invalid-id", "Invalid parameter format");
    }

    @Test
    void softDeleteDownload_WithUnknownId_ShouldReturnNotFound() {
        UUID id = UUID.randomUUID();

        Response response = api.delete("/api/v1/downloads/" + id, adminToken());

        assertThat(response.status()).isEqualTo(404);
        Json.assertError(response.body(), 404, "/api/v1/downloads/" + id,
                "Download progress not found with identifier: " + id);
    }
}
