package org.tailkeep.api.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.tailkeep.api.support.Json;
import org.tailkeep.api.support.Shapes;
import org.tailkeep.api.support.Wire;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadProgressListenerIntegrationTest extends MessagingIntegrationTest {

    /** A job that has its video, as it does once the metadata has been recorded. */
    private UUID jobWithVideo(String youtubeId) {
        UUID channelId = db.insertChannel("Some Uploader", "UC123");
        UUID videoId = db.insertVideo(channelId, youtubeId, "A title", LocalDateTime.of(2025, 1, 1, 12, 0));
        return db.insertJob(videoId, "https://www.youtube.com/watch?v=" + youtubeId);
    }

    @Test
    void progress_ShouldCreateThenUpdateTheDownload() {
        String token = userToken();
        UUID jobId = jobWithVideo("abc123");

        wire.send(Wire.DOWNLOAD_PROGRESS,
                progress(jobId, "abc123", "download", false, 12.5, "100.00MiB", "2.00MiB/s", "00:30"));

        await(() -> api.get("/api/v1/downloads/" + jobId, token).status() == 200);
        JsonNode created = Shapes.download(download(token, jobId));
        assertThat(created.get("id").asText()).isEqualTo(jobId.toString());
        assertThat(created.get("video").get("youtubeId").asText()).isEqualTo("abc123");
        assertThat(created.get("status").asText()).isEqualTo("download");
        assertThat(created.get("hasEnded").asBoolean()).isFalse();
        assertThat(created.get("progress").asDouble()).isEqualTo(12.5);
        assertThat(created.get("size").asText()).isEqualTo("100.00MiB");
        assertThat(created.get("speed").asText()).isEqualTo("2.00MiB/s");
        assertThat(created.get("eta").asText()).isEqualTo("00:30");
        assertThat(created.get("completedAt").isNull()).isTrue();

        wire.send(Wire.DOWNLOAD_PROGRESS,
                progress(jobId, "abc123", "download", false, 60.0, "100.00MiB", "3.00MiB/s", "00:10"));

        await(() -> download(token, jobId).get("progress").asDouble() == 60.0);
        JsonNode updated = download(token, jobId);
        assertThat(updated.get("speed").asText()).isEqualTo("3.00MiB/s");
        assertThat(updated.get("eta").asText()).isEqualTo("00:10");
        assertThat(db.count("download_progress")).isEqualTo(1);
    }

    @Test
    void endedProgress_ShouldCompleteTheDownload() {
        String token = userToken();
        UUID jobId = jobWithVideo("abc123");
        wire.send(Wire.DOWNLOAD_PROGRESS,
                progress(jobId, "abc123", "download", false, 90.0, "100.00MiB", "2.00MiB/s", "00:03"));
        await(() -> api.get("/api/v1/downloads/" + jobId, token).status() == 200);

        wire.send(Wire.DOWNLOAD_PROGRESS,
                progress(jobId, "abc123", "finished", true, 99.0, "98.00MiB", "2.00MiB/s", "00:01"));

        await(() -> download(token, jobId).get("hasEnded").asBoolean());
        JsonNode done = Shapes.download(download(token, jobId));
        assertThat(done.get("status").asText()).isEqualTo("done");
        assertThat(done.get("progress").asDouble()).isEqualTo(100.0);
        assertThat(done.get("size").asText()).isEqualTo("98.00MiB");
        assertThat(done.get("speed").asText()).isEqualTo("0B/s");
        assertThat(done.get("eta").asText()).isEqualTo("00:00");
        assertThat(done.get("completedAt").isNull()).isFalse();
        assertThat(api.get("/api/v1/videos/" + done.get("video").get("id").asText(), token).json()
                .get("doneDownloading").asBoolean()).isTrue();
    }

    @Test
    void progressForAnUnknownJob_ShouldNotBlockLaterMessages() {
        String token = userToken();
        UUID unknownJob = UUID.randomUUID();
        UUID jobId = jobWithVideo("abc123");

        wire.send(Wire.DOWNLOAD_PROGRESS,
                progress(unknownJob, "other", "download", false, 5.0, "1.00MiB", "1.00MiB/s", "00:01"));
        wire.send(Wire.DOWNLOAD_PROGRESS,
                progress(jobId, "abc123", "download", false, 12.5, "100.00MiB", "2.00MiB/s", "00:30"));

        await(() -> api.get("/api/v1/downloads/" + jobId, token).status() == 200);
        Json.assertError(api.get("/api/v1/downloads/" + unknownJob, token).body(), 404,
                "/api/v1/downloads/" + unknownJob, "Download progress not found with identifier: " + unknownJob);
    }
}
