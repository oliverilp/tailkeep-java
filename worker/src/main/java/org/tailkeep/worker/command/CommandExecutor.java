package org.tailkeep.worker.command;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class CommandExecutor {
    private final String mediaPath;
    private final AtomicReference<Process> currentProcess = new AtomicReference<>();

    // public CommandExecutor(@Value("${MEDIA_PATH}") String mediaPath) {
    public CommandExecutor() {
        String mediaPath = System.getenv("MEDIA_PATH");
        if (mediaPath == null || mediaPath.isBlank()) {
            mediaPath = System.getProperty("user.home") + "/Videos/";
            log.warn("MEDIA_PATH environment variable not set, using default: " + mediaPath);
        }
        this.mediaPath = mediaPath;
    }

    public CompletableFuture<Void> execute(List<String> args, CommandOutput onDataCallback) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<String> commandWithArgs = new java.util.ArrayList<>();
                commandWithArgs.add("yt-dlp");
                commandWithArgs.addAll(args);

                ProcessBuilder processBuilder = new ProcessBuilder(commandWithArgs)
                        .directory(new File(mediaPath));

                Process process = processBuilder.start();
                if (!currentProcess.compareAndSet(null, process)) {
                    throw new IllegalStateException("Another command is already running");
                }

                // Handle stdout and stderr in parallel
                CompletableFuture<Void> outputHandling = CompletableFuture.allOf(
                        handleStream(process.getInputStream(), onDataCallback::onData),
                        handleStream(process.getErrorStream(), line -> log.error("CMD Error: " + line)));

                // Wait for output handling to complete and check process exit code
                outputHandling.join();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new RuntimeException("Process exited with code " + exitCode);
                }
                log.info("Command process completed successfully");

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Command execution failed", e);
            } finally {
                currentProcess.set(null);
            }
        });
    }

    private CompletableFuture<Void> handleStream(InputStream stream, Consumer<String> lineHandler) {
        return CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                reader.lines().forEach(lineHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void kill() {
        Process process = currentProcess.getAndSet(null);
        if (process != null && process.isAlive()) {
            process.destroy();
            System.out.println("Process killed.");
        } else {
            System.out.println("No active process to kill.");
        }
    }
}
