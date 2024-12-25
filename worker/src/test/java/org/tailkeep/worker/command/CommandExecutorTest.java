package org.tailkeep.worker.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

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
        List<String> args;

        // Use different commands based on OS
        String command = "echo";
        args = List.of("test");

        // When
        assertThat(commandExecutor.execute(command, args, capturedOutput::set)
                .thenApply(unused -> capturedOutput.get().trim())
                .join())
                .isEqualTo("test");
    }

    @Test
    void kill_ShouldTerminateRunningProcess() {
        // Given
        AtomicReference<String> output = new AtomicReference<>();
        List<String> args;
        String command;

        // Use a long-running command that we can interrupt
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            command = "cmd";
            args = List.of("/c", "ping", "-t", "localhost");
        } else {
            command = "/bin/sh";
            args = List.of("-c", "sleep 10");
        }

        // When
        CompletableFuture<Void> future = commandExecutor.execute(command, args, output::set);

        // Give the process a moment to start
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        commandExecutor.kill();

        // Then
        assertThatThrownBy(future::join)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("Process exited with code");
    }
}
