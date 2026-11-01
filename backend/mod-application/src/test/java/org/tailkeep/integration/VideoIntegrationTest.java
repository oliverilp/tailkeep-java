package org.tailkeep.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.tailkeep.support.Api.Response;
import org.tailkeep.support.Json;
import org.tailkeep.support.Shapes;
import org.tailkeep.support.TestDb.Download;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoIntegrationTest extends BaseIntegrationTest {

    @Test
    void getAllVideos_WithoutAuth_ShouldFail() {
        Response response = api.get("/api/v1/videos");

        assertThat(response.status()).isEqualTo(401);
        Json.assertError(response.body(), 401, "/api/v1/videos", "Authentication is required to access this resource");
    }

    @Test
    void getAllVideos_WithoutData_ShouldReturnEmptyList() {
        Response response = api.get("/api/v1/videos", userToken());

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("[]");
    }

    @Test
    void getAllVideos_ShouldReturnNewestFirst() {
        Download first = db.downloading(1);
        Download second = db.downloading(2);
        Download third = db.downloading(3);

        Response response = api.get("/api/v1/videos", userToken());

        assertThat(response.status()).isEqualTo(200);
        JsonNode videos = response.json();
        assertThat(videos.isArray()).isTrue();
        assertThat(videos).hasSize(3);
        videos.forEach(video -> Shapes.video(video, false));
        assertThat(Json.ids(videos)).containsExactly(
                third.videoId().toString(), second.videoId().toString(), first.videoId().toString());
        assertThat(videos.get(0).get("title").asText()).isEqualTo("Video 3");
    }

    @Test
    void getVideoById_ShouldReturnAllFields() {
        Download download = db.downloading(1);

        Response response = api.get("/api/v1/videos/" + download.videoId(), userToken());

        assertThat(response.status()).isEqualTo(200);
        JsonNode video = Shapes.video(response.json(), true);
        assertThat(video.get("id").asText()).isEqualTo(download.videoId().toString());
        assertThat(video.get("youtubeId").asText()).isEqualTo("video1");
        assertThat(video.get("url").asText()).isEqualTo("https://www.youtube.com/watch?v=video1");
        assertThat(video.get("title").asText()).isEqualTo("Video 1");
        assertThat(video.get("durationString").asText()).isEqualTo("03:32");
        assertThat(video.get("duration").asDouble()).isEqualTo(212.0);
        assertThat(video.get("thumbnailUrl").asText()).isEqualTo("https://img.example.com/video1.jpg");
        assertThat(video.get("description").asText()).isEqualTo("A description");
        assertThat(video.get("viewCount").asLong()).isEqualTo(1000);
        assertThat(video.get("commentCount").asLong()).isEqualTo(50);
        assertThat(video.get("filename").asText()).isEqualTo("video1.mp4");
        assertThat(video.get("channel").get("id").asText()).isEqualTo(download.channelId().toString());
        assertThat(video.get("channel").get("name").asText()).isEqualTo("Channel 1");
        assertThat(video.get("channel").get("youtubeId").asText()).isEqualTo("UCchannel1");
        assertThat(video.get("channel").get("channelUrl").asText())
                .isEqualTo("https://www.youtube.com/channel/UCchannel1");
    }

    @Test
    void getVideoById_WhileDownloading_ShouldNotBeDone() {
        Download download = db.downloading(1);

        JsonNode video = api.get("/api/v1/videos/" + download.videoId(), userToken()).json();

        assertThat(video.get("doneDownloading").asBoolean()).isFalse();
    }

    @Test
    void getVideoById_WhenEveryDownloadHasEnded_ShouldBeDone() {
        Download download = db.finished(1);

        JsonNode video = api.get("/api/v1/videos/" + download.videoId(), userToken()).json();

        assertThat(video.get("doneDownloading").asBoolean()).isTrue();
    }

    @Test
    void getVideoById_WithoutAuth_ShouldFail() {
        Response response = api.get("/api/v1/videos/" + UUID.randomUUID());

        assertThat(response.status()).isEqualTo(401);
    }

    @Test
    void getVideoById_WithMalformedId_ShouldFail() {
        Response response = api.get("/api/v1/videos/invalid-id", userToken());

        assertThat(response.status()).isEqualTo(400);
        Json.assertError(response.body(), 400, "/api/v1/videos/invalid-id", "Invalid parameter format");
    }

    @Test
    void getVideoById_WithUnknownId_ShouldReturnNotFound() {
        UUID id = UUID.randomUUID();

        Response response = api.get("/api/v1/videos/" + id, userToken());

        assertThat(response.status()).isEqualTo(404);
        Json.assertError(response.body(), 404, "/api/v1/videos/" + id,
                "Video not found with identifier: " + id);
    }
}
