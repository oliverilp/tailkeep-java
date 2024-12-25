package org.tailkeep.worker.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandExecutorTest {

    private CommandExecutor commandExecutor;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        System.setProperty("user.home", tempDir.toString());
        commandExecutor = new CommandExecutor();
    }

    @Test
    void execute_ShouldCaptureOutput() {
        // Given
        AtomicReference<String> capturedOutput = new AtomicReference<>();
        List<String> command = OS.WINDOWS.isCurrentOs() 
            ? List.of("cmd", "/c", "echo", "test echo cmd")
            : List.of("sh", "-c", "echo test echo cmd");

        // When
        CompletableFuture<Void> future = commandExecutor.execute(command, capturedOutput::set);

        // Then
        future.join();
        assertThat(capturedOutput.get()).contains("test echo cmd");
    }

    @Test
    void kill_ShouldTerminateRunningProcess() throws Exception {
        // Given
        List<String> command = OS.WINDOWS.isCurrentOs()
            ? List.of("cmd", "/c", "ping", "-t", "localhost")
            : List.of("sh", "-c", "sleep 10");

        AtomicReference<String> output = new AtomicReference<>();
        
        // When
        CompletableFuture<Void> future = commandExecutor.execute(command, output::set);
        Thread.sleep(1000); // Give process time to start
        commandExecutor.kill();

        // Then
        assertThatThrownBy(future::join)
            .isInstanceOf(CompletionException.class)
            .hasRootCauseInstanceOf(InterruptedException.class);
    }

    @Test
    void execute_WhenAnotherCommandIsRunning_ShouldThrowException() {
        // Given
        List<String> longRunningCommand = OS.WINDOWS.isCurrentOs()
            ? List.of("cmd", "/c", "ping", "-t", "localhost")
            : List.of("sh", "-c", "sleep 10");

        List<String> secondCommand = OS.WINDOWS.isCurrentOs()
            ? List.of("cmd", "/c", "echo", "test")
            : List.of("sh", "-c", "echo test");

        // When
        commandExecutor.execute(longRunningCommand, s -> {});

        // Then
        assertThatThrownBy(() -> commandExecutor.execute(secondCommand, s -> {}))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Command already running");
    }
}
