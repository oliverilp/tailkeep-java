package org.tailkeep.worker.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tailkeep.worker.command.CommandExecutor;
import org.tailkeep.worker.command.CommandOutput;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetadataFetcherTest {

    @Mock
    private CommandExecutor commandExecutor;

    private MetadataFetcher metadataFetcher;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        metadataFetcher = new MetadataFetcher(commandExecutor, objectMapper);
    }

    @Test
    void fetch_ShouldParseMetadataCorrectly() {
        // Given
        String jsonResponse = """
            {
                "id": "dQw4w9WgXcQ",
                "title": "Rick Astley - Never Gonna Give You Up",
                "channel": "Rick Astley",
                "duration": 213,
                "view_count": 1234567,
                "uploader": "Rick Astley"
            }
            """;

        when(commandExecutor.execute(eq(List.of("-j", "https://youtube.com/watch?v=dQw4w9WgXcQ")), any()))
            .thenAnswer(inv -> {
                CommandOutput output = inv.getArgument(1);
                output.onData(jsonResponse);
                return CompletableFuture.completedFuture(null);
            });

        // When
        CompletableFuture<Metadata> futureMetadata = metadataFetcher.fetch("https://youtube.com/watch?v=dQw4w9WgXcQ");
        Metadata metadata = futureMetadata.join();

        // Then
        assertThat(metadata).isNotNull();
        assertThat(metadata.title()).isEqualTo("Rick Astley - Never Gonna Give You Up");
        assertThat(metadata.youtubeId()).isEqualTo("dQw4w9WgXcQ");
        assertThat(metadata.uploader()).isEqualTo("Rick Astley");
    }
}
