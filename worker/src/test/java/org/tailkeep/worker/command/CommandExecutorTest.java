package org.tailkeep.worker.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tailkeep.worker.config.MediaProperties;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandExecutorTest {

    @Mock
    private ProcessFactory processFactory;
    @Mock
    private Process process;
    
    private CommandExecutor commandExecutor;
    private File mediaPath;

    @BeforeEach
    void setUp() throws Exception {
        MediaProperties mediaProperties = new MediaProperties();
        commandExecutor = new CommandExecutor(processFactory, mediaProperties);
        mediaPath = new File(mediaProperties.getPath());
        
        // Default mock behavior
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(process.waitFor()).thenReturn(0);
    }

    @Test
    void execute_ShouldCaptureOutput() throws Exception {
        // Given
        List<String> command = List.of("test", "command");
        List<String> expectedCommand = List.of("yt-dlp", "test", "command");
        String expectedOutput = "test output\n";
        AtomicReference<String> capturedOutput = new AtomicReference<>();
        
        when(processFactory.createProcess(expectedCommand, mediaPath)).thenReturn(process);
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(expectedOutput.getBytes()));

        // When
        commandExecutor.execute(command, capturedOutput::set).join();

        // Then
        assertThat(capturedOutput.get()).isEqualTo(expectedOutput.trim());
    }

    @Test
    void execute_WhenProcessFails_ShouldThrowException() throws Exception {
        // Given
        List<String> command = List.of("test", "command");
        List<String> expectedCommand = List.of("yt-dlp", "test", "command");
        
        when(processFactory.createProcess(expectedCommand, mediaPath)).thenReturn(process);
        when(process.waitFor()).thenReturn(1);

        // When/Then
        assertThatThrownBy(() -> commandExecutor.execute(command, s -> {}).join())
            .hasRootCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Process exited with code 1");
    }

    @Test
    void execute_WhenAnotherCommandIsRunning_ShouldThrowException() throws Exception {
        // Given
        List<String> command = List.of("test", "command");
        List<String> expectedCommand = List.of("yt-dlp", "test", "command");
        
        when(processFactory.createProcess(expectedCommand, mediaPath)).thenReturn(process);
        when(process.waitFor()).thenAnswer(inv -> {
            Thread.sleep(500);
            return 0;
        });

        // When
        CompletableFuture<Void> future1 = commandExecutor.execute(command, s -> {});
        Thread.sleep(100); // Ensure first command is running

        // Then
        CompletableFuture<Void> future2 = commandExecutor.execute(command, s -> {});
        assertThatThrownBy(future2::join)
            .hasRootCauseInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Another command is already running");

        future1.join(); // Clean up
    }

    @Test
    void kill_ShouldTerminateRunningProcess() throws Exception {
        // Given
        List<String> command = List.of("test", "command");
        List<String> expectedCommand = List.of("yt-dlp", "test", "command");
        
        when(processFactory.createProcess(expectedCommand, mediaPath)).thenReturn(process);
        when(process.waitFor()).thenAnswer(inv -> {
            Thread.sleep(500);
            return 0;
        });
        when(process.isAlive()).thenReturn(true);

        // When
        commandExecutor.execute(command, s -> {});
        Thread.sleep(100); // Ensure process is running
        commandExecutor.kill();

        // Then
        verify(process).destroy();
    }
}
