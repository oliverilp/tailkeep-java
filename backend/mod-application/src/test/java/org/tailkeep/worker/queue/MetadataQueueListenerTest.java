package org.tailkeep.worker.queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tailkeep.api.message.MetadataRequestMessage;
import org.tailkeep.domain.download.FetchMetadata;
import org.tailkeep.domain.download.MetadataRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MetadataQueueListenerTest {

    @Mock
    private FetchMetadata fetchMetadata;

    @Test
    void onMetadataJob_ShouldFetchTheMetadataForTheRequest() {
        UUID jobId = UUID.randomUUID();
        MetadataQueueListener listener = new MetadataQueueListener(fetchMetadata);

        listener.onMetadataJob(new MetadataRequestMessage(jobId, "https://youtube.com/watch?v=test"));

        ArgumentCaptor<MetadataRequest> request = ArgumentCaptor.forClass(MetadataRequest.class);
        verify(fetchMetadata).execute(request.capture());
        assertThat(request.getValue()).isEqualTo(new MetadataRequest(jobId, "https://youtube.com/watch?v=test"));
    }

    @Test
    void onMetadataJob_WhenTheFetchFails_ShouldNotThrow() {
        doThrow(new RuntimeException("Test exception")).when(fetchMetadata).execute(any());
        MetadataQueueListener listener = new MetadataQueueListener(fetchMetadata);

        assertThatCode(() -> listener.onMetadataJob(
                new MetadataRequestMessage(UUID.randomUUID(), "https://youtube.com/watch?v=test")))
                .doesNotThrowAnyException();
    }
}
