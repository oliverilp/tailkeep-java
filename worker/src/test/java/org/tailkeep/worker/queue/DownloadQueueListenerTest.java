package org.tailkeep.worker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.tailkeep.worker.config.KafkaTopicNames;
import org.tailkeep.worker.download.DownloadProgressMessage;
import org.tailkeep.worker.download.DownloadRequestMessage;
import org.tailkeep.worker.download.DownloadService;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadQueueListenerTest {

    @Mock
    private DownloadService downloadService;

    @Mock
    private KafkaTemplate<String, DownloadProgressMessage> kafkaTemplate;

    @Captor
    private ArgumentCaptor<DownloadProgressMessage> progressMessageCaptor;

    @Mock
    private ObjectMapper objectMapper;

    private DownloadQueueListener listener;

    @BeforeEach
    void setUp() {
        listener = new DownloadQueueListener(downloadService, objectMapper, kafkaTemplate);
    }

    @Test
    void onDownloadJob_ShouldProcessDownloadAndSendProgressUpdates() {
        // Given
        String jobId = "job123";
        String videoId = "video123";
        String url = "https://youtube.com/watch?v=test";
        String filename = "test.mp4";

        DownloadRequestMessage message = new DownloadRequestMessage(jobId, videoId, url, filename);
        DownloadProgressMessage progressMessage = new DownloadProgressMessage(
                videoId, jobId, "downloading", false, 50.0, "100MB", "1MB/s", "00:30"
        );
        DownloadProgressMessage completedMessage = new DownloadProgressMessage(
                videoId, jobId, "finished", true, 100.0, "100MB", "1MB/s", "00:00"
        );

        when(downloadService.processDownload(
                eq(jobId), eq(videoId), eq(url), eq(filename), any()
        )).thenReturn(CompletableFuture.completedFuture(completedMessage));

        // When
        listener.onDownloadJob(message);

        // Then
        verify(downloadService).processDownload(
                eq(jobId), eq(videoId), eq(url), eq(filename), any()
        );

        verify(kafkaTemplate, atLeast(1)).send(
                eq(KafkaTopicNames.DOWNLOAD_PROGRESS),
                any(DownloadProgressMessage.class)
        );
    }

    @Test
    void onDownloadJob_WhenExceptionOccurs_ShouldHandleGracefully() {
        // Given
        DownloadRequestMessage message = new DownloadRequestMessage(
                "job123", "video123", "https://youtube.com/watch?v=test", "test.mp4"
        );

        when(downloadService.processDownload(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Test exception"));

        // When
        listener.onDownloadJob(message);

        // Then
        verify(downloadService).processDownload(any(), any(), any(), any(), any());
        verifyNoMoreInteractions(kafkaTemplate);
    }
} 
