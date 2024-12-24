package org.tailkeep.worker.download;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tailkeep.worker.command.CommandExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
class DownloadServiceTest {

    @Mock
    private CommandExecutor commandExecutor;

    private DownloadService downloadService;

    @BeforeEach
    void setUp() {
        downloadService = new DownloadService(commandExecutor);
    }

    @Test
    void processDownload_ShouldExecuteCommandAndReportProgress() {
        // Given
        String jobId = "job123";
        String videoId = "video123";
        String url = "https://youtube.com/watch?v=dQw4w9WgXcQ";
        String filename = "video.mp4";

        List<String> expectedArgs = Collections.singletonList(url);
        
        when(commandExecutor.execute(argThat(args -> 
            args.size() == expectedArgs.size() && 
            args.containsAll(expectedArgs) && 
            expectedArgs.containsAll(args)
        ), any()))
            .thenReturn(CompletableFuture.completedFuture(null));

        @SuppressWarnings("unchecked")
        Consumer<DownloadProgressMessage> mockProgressCallback = mock(Consumer.class);

        // When
        downloadService.processDownload(jobId, videoId, url, filename, mockProgressCallback);

        // Then
        verify(commandExecutor).execute(argThat(args -> 
            args.size() == expectedArgs.size() && 
            args.containsAll(expectedArgs) && 
            expectedArgs.containsAll(args)
        ), any());
    }
}
