package org.tailkeep.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.tailkeep.support.Json;
import org.tailkeep.support.Shapes;
import org.tailkeep.support.Wire;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The steps of the pipeline that don't run yt-dlp. The test sends the messages those steps would receive from it.
 * The URL is checked with a real HEAD request, so this needs network access.
 */
class DownloadPipelineIntegrationTest extends MessagingIntegrationTest {

    @Test
    void startDownload_ShouldPublishAMetadataRequestWithACleanUrl() {
        String token = adminToken();

        int status = api.post("/api/v1/downloads",
                "{\"url\":\"https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=PLabc&t=5\"}", token).status();

        assertThat(status).isEqualTo(202);
        UUID jobId = db.onlyJobId();
        JsonNode request = wire.awaitMessageForJob(Wire.METADATA_QUEUE, jobId);
        Json.assertKeys(request, "jobId", "url");
        assertThat(request.get("url").asText()).isEqualTo("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
    }

    @Test
    void download_ShouldGoFromRequestToFinished() {
        String token = adminToken();
        api.post("/api/v1/downloads", "{\"url\":\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\"}", token);
        UUID jobId = db.onlyJobId();
        wire.awaitMessageForJob(Wire.METADATA_QUEUE, jobId);

        wire.send(Wire.METADATA_RESULTS, metadataResult(jobId, "dQw4w9WgXcQ", "Never gonna", 5000));
        JsonNode downloadRequest = wire.awaitMessageForJob(Wire.DOWNLOAD_QUEUE, jobId);
        assertThat(downloadRequest.get("videoId").asText()).isEqualTo("dQw4w9WgXcQ");

        wire.send(Wire.DOWNLOAD_PROGRESS,
                progress(jobId, "dQw4w9WgXcQ", "download", false, 50.0, "10.00MiB", "1.00MiB/s", "00:05"));
        await(() -> api.get("/api/v1/downloads/" + jobId, token).status() == 200);
        assertThat(download(token, jobId).get("hasEnded").asBoolean()).isFalse();

        wire.send(Wire.DOWNLOAD_PROGRESS,
                progress(jobId, "dQw4w9WgXcQ", "finished", true, 100.0, "10.00MiB", "1.00MiB/s", "00:00"));
        await(() -> download(token, jobId).get("hasEnded").asBoolean());

        JsonNode finished = Shapes.download(download(token, jobId));
        assertThat(finished.get("status").asText()).isEqualTo("done");
        assertThat(finished.get("video").get("title").asText()).isEqualTo("Never gonna");
        JsonNode dashboard = api.get("/api/v1/downloads/dashboard", token).json();
        assertThat(dashboard.get("queueInfo").get("finished").asLong()).isEqualTo(1);
        assertThat(dashboard.get("queueInfo").get("active").asLong()).isZero();
    }
}
