package org.tailkeep.domain.download.usecase;

import org.junit.jupiter.api.Test;
import org.tailkeep.domain.download.model.MetadataRequest;
import org.tailkeep.domain.download.model.MetadataResult;
import org.tailkeep.domain.download.model.VideoMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FetchMetadataImplTest {
    static VideoMetadata metadata(String youtubeId) {
        return new VideoMetadata(youtubeId, "https://www.youtube.com/watch?v=" + youtubeId, "A title", "Uploader",
                "UC1", "https://www.youtube.com/channel/UC1", "10:00", 600.0, "https://img/" + youtubeId + ".jpg",
                "A description", 1000, 100, youtubeId + ".mp4");
    }

    private final List<MetadataResult> published = new ArrayList<>();

    @Test
    void execute_ShouldPublishWhatTheProviderFoundForTheJob() {
        UUID jobId = UUID.randomUUID();
        List<String> asked = new ArrayList<>();
        FetchMetadataImpl fetchMetadata = new FetchMetadataImpl(url -> {
            asked.add(url);
            return metadata("abc");
        }, published::add);

        fetchMetadata.execute(new MetadataRequest(jobId, "https://www.youtube.com/watch?v=abc"));

        assertThat(asked).containsExactly("https://www.youtube.com/watch?v=abc");
        assertThat(published).containsExactly(new MetadataResult(jobId, metadata("abc")));
    }

    @Test
    void execute_WhenTheProviderFails_ShouldPublishNothing() {
        FetchMetadataImpl fetchMetadata = new FetchMetadataImpl(url -> {
            throw new IllegalStateException("no metadata");
        }, published::add);

        assertThatThrownBy(() -> fetchMetadata.execute(new MetadataRequest(UUID.randomUUID(), "https://x.com/v")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(published).isEmpty();
    }
}
