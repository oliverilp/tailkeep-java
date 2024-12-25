package org.tailkeep.worker.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Component
@Slf4j
public class CommandExecutor {
    private final File mediaPath;
    private final AtomicReference<Process> currentProcess = new AtomicReference<>();
    private final ProcessFactory processFactory;

    public CommandExecutor() {
        this(new DefaultProcessFactory());
    }

    CommandExecutor(ProcessFactory processFactory) {
        this.processFactory = processFactory;
        String mediaPathStr = System.getenv("MEDIA_PATH");
        if (mediaPathStr == null || mediaPathStr.isBlank()) {
            mediaPathStr = System.getProperty("user.home") + "/Videos/";
            log.warn("MEDIA_PATH environment variable not set, using default: {}", mediaPathStr);
        }
        this.mediaPath = new File(mediaPathStr);
    }

    public CompletableFuture<Void> execute(List<String> args, CommandOutput onDataCallback) {
        String command = "yt-dlp";
        return this.execute(command, args, onDataCallback);
    }

    public CompletableFuture<Void> execute(String command, List<String> args, CommandOutput onDataCallback) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<String> commandWithArgs = new ArrayList<>();
                commandWithArgs.add(command);
                commandWithArgs.addAll(args);

                log.info("Executing command: {}", commandWithArgs);

                Process process = processFactory.createProcess(commandWithArgs, mediaPath);
                if (!currentProcess.compareAndSet(null, process)) {
                    process.destroy();
                    throw new IllegalStateException("Another command is already running");
                }

                // Handle stdout and stderr in parallel
                CompletableFuture<Void> outputHandling = CompletableFuture.allOf(
                        handleStream(process.getInputStream(), onDataCallback::onData),
                        handleStream(process.getErrorStream(), line -> log.error("CMD Error: {}", line)));

                // Wait for output handling to complete and check process exit code
                outputHandling.join();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new RuntimeException("Process exited with code " + exitCode);
                }
                log.info("Command process completed successfully");

            } catch (IOException | InterruptedException e) {
                log.error("Command execution failed", e);
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
                log.error("Failed to read process output", e);
            }
        });
    }

    public void kill() {
        Process process = currentProcess.getAndSet(null);
        if (process != null && process.isAlive()) {
            process.destroy();
            log.info("Process killed.");
        } else {
            log.warn("No active process to kill.");
        }
    }
}
