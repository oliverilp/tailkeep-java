package org.tailkeep.adapter.ytdlp.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tailkeep.adapter.ytdlp.config.YtDlpProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Runs yt-dlp in the media directory and returns when it exits. Only one process runs at a time in the
 * application.
 */
@Component
@Slf4j
public class YtDlpRunner {
    private static final String COMMAND = "yt-dlp";

    private final File mediaPath;
    private final ProcessFactory processFactory;
    private final AtomicReference<Process> currentProcess = new AtomicReference<>();

    public YtDlpRunner(ProcessFactory processFactory, YtDlpProperties properties) {
        this.processFactory = processFactory;
        this.mediaPath = new File(properties.getPath());
        log.info("Using media path: {}", mediaPath);
    }

    /** Passes each line yt-dlp prints on stdout to {@code onStdoutLine}. */
    public void run(List<String> args, Consumer<String> onStdoutLine) {
        List<String> command = new ArrayList<>();
        command.add(COMMAND);
        command.addAll(args);
        log.info("Executing command: {}", command);

        Process process = null;
        try {
            process = processFactory.createProcess(command, mediaPath);
            if (!currentProcess.compareAndSet(null, process)) {
                process.destroy();
                process = null;
                throw new IllegalStateException("Another command is already running");
            }

            Thread stderr = logStderr(process.getErrorStream());
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(onStdoutLine);
            }
            stderr.join();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Process exited with code " + exitCode);
            }
            log.info("Command process completed successfully");
        } catch (IOException e) {
            log.error("Command execution failed", e);
            throw new RuntimeException("Command execution failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Command execution failed", e);
            throw new RuntimeException("Command execution failed", e);
        } finally {
            if (process != null) {
                currentProcess.compareAndSet(process, null);
                if (process.isAlive()) {
                    process.destroy();
                }
            }
        }
    }

    private Thread logStderr(InputStream stream) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                reader.lines().forEach(line -> log.error("CMD Error: {}", line));
            } catch (IOException e) {
                log.error("Failed to read process output", e);
            }
        }, "yt-dlp-stderr");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
}
