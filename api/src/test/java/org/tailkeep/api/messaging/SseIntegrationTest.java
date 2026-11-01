package org.tailkeep.api.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.tailkeep.api.support.Json;
import org.tailkeep.api.support.Shapes;
import org.tailkeep.api.support.TestDb.Download;
import org.tailkeep.api.support.Wire;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SseIntegrationTest extends MessagingIntegrationTest {

    @Test
    void subscribe_WithoutAuth_ShouldFail() {
        assertThat(api.get("/api/v1/sse/subscribe").status()).isEqualTo(401);
    }

    @Test
    void progress_ShouldBeBroadcastToSubscribers() throws Exception {
        String token = userToken();
        Download download = db.downloading(1);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl() + "/api/v1/sse/subscribe"))
                .header("Authorization", "Bearer " + token)
                .build();

        // The response only starts with the first event, so keep sending until a subscriber has received one.
        CompletableFuture<HttpResponse<Stream<String>>> pending = HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofLines());
        String progress = progress(download.jobId(), download.youtubeId(), "download", false, 77.5, "10.00MiB",
                "5.00MiB/s", "00:02");
        await(() -> {
            wire.send(Wire.DOWNLOAD_PROGRESS, progress);
            return pending.isDone();
        });

        HttpResponse<Stream<String>> response = pending.get();
        try (Stream<String> lines = response.body()) {
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Content-Type").orElse("")).startsWith("text/event-stream");

            String data = CompletableFuture.supplyAsync(() -> lines.filter(line -> line.startsWith("data:"))
                    .findFirst().orElseThrow()).get(30, TimeUnit.SECONDS);

            JsonNode event = Shapes.download(Json.parse(data.substring("data:".length())));
            assertThat(event.get("id").asText()).isEqualTo(download.jobId().toString());
            assertThat(event.get("progress").asDouble()).isEqualTo(77.5);
            assertThat(event.get("speed").asText()).isEqualTo("5.00MiB/s");
            assertThat(event.get("eta").asText()).isEqualTo("00:02");
        }
    }
}
