package org.tailkeep.worker.queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tailkeep.api.message.DownloadRequestMessage;
import org.tailkeep.domain.download.DownloadRequest;
import org.tailkeep.domain.download.RunDownload;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DownloadQueueListenerTest {

    @Mock
    private RunDownload runDownload;

    @Test
    void onDownloadJob_ShouldRunTheDownloadForTheRequest() {
        UUID jobId = UUID.randomUUID();
        DownloadQueueListener listener = new DownloadQueueListener(runDownload);

        listener.onDownloadJob(new DownloadRequestMessage(jobId, "video123", "https://youtube.com/watch?v=test", "test.mp4"));

        ArgumentCaptor<DownloadRequest> request = ArgumentCaptor.forClass(DownloadRequest.class);
        verify(runDownload).execute(request.capture());
        assertThat(request.getValue())
                .isEqualTo(new DownloadRequest(jobId, "video123", "https://youtube.com/watch?v=test", "test.mp4"));
    }

    @Test
    void onDownloadJob_WhenTheDownloadFails_ShouldNotThrow() {
        doThrow(new RuntimeException("Test exception")).when(runDownload).execute(any());
        DownloadQueueListener listener = new DownloadQueueListener(runDownload);

        assertThatCode(() -> listener.onDownloadJob(
                new DownloadRequestMessage(UUID.randomUUID(), "video123", "https://youtube.com/watch?v=test", "test.mp4")))
                .doesNotThrowAnyException();
    }
}
