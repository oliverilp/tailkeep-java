package org.tailkeep.worker.download;

import org.tailkeep.worker.command.CommandExecutor;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Consumer;

@Slf4j
public class Downloader {
    // private static final Pattern DOWNLOAD_REGEX = Pattern.compile(
    //         "\\[(.*?)\\]\\s+(\\d+\\.?\\d*%)\\s+of\\s+~?\\s+(\\d+\\.\\d+\\w{1,3})\\s+(?:in \\d+:\\d+(?::\\d+)?\\s+)?at\\s+(\\d+\\.\\d+\\w{1,3}/s)(?:\\s+ETA\\s+(?:(\\d+:\\d+(?::\\d+)?)|Unknown))?\\s+\\(frag (\\d+)/\\d+\\)");
    // private static final Pattern CATEGORY_REGEX = Pattern.compile("^(?:\\[)(.*?)(?:\\])");

    private static final Pattern DOWNLOAD_PROGRESS_REGEX = Pattern.compile("(\\d+\\.?\\d*)%");
    private static final Pattern DOWNLOAD_SIZE_REGEX = Pattern.compile("of\\s+~?\\s+(\\d+\\.\\d+\\w{1,3})");
    private static final Pattern DOWNLOAD_SPEED_REGEX = Pattern.compile("at\\s+(\\d+\\.\\d+\\w{1,3}/s)");
    private static final Pattern DOWNLOAD_ETA_REGEX = Pattern.compile("ETA\\s+(\\d+:\\d+(?::\\d+)?)");
    // private static final Pattern DOWNLOAD_FRAG_REGEX = Pattern.compile("\\(frag (\\d+)/\\d+\\)");
    private static final Pattern CATEGORY_REGEX = Pattern.compile("^(?:\\[)(.*?)(?:\\])");

    private final CommandExecutor cmd;
    private final String videoId;
    private final String jobId;
    private final String url;
    private final String filename;
    private final String mediaPath;

    private String status = null;
    private boolean hasEnded = false;
    private double progress = 0;
    private String size = null;
    private String speed = null;
    private String eta = null;

    public Downloader(CommandExecutor cmd, String videoId, String jobId, String url, String filename) {
        this.cmd = cmd;
        this.videoId = videoId;
        this.jobId = jobId;
        this.url = url;
        this.filename = filename;
        this.mediaPath = System.getenv("MEDIA_PATH");
    }

    public DownloadProgress getDownloadProgress() {
        return new DownloadProgress(videoId, jobId, status, hasEnded, progress, size, speed, eta);
    }

    public DownloadProgress onOutput(String text) {
        log.info(text);

        // Check status
        Matcher statusMatcher = CATEGORY_REGEX.matcher(text);
        if (statusMatcher.find()) {
            status = statusMatcher.group(1);
        }

        // Check progress
        Matcher progressMatcher = DOWNLOAD_PROGRESS_REGEX.matcher(text);
        if (progressMatcher.find()) {
            String progressStr = progressMatcher.group(1);
            double percentage = Double.parseDouble(progressStr);
            progress = percentage;
        }

        // Check size
        Matcher sizeMatcher = DOWNLOAD_SIZE_REGEX.matcher(text);
        if (sizeMatcher.find()) {
            String totalSize = sizeMatcher.group(1);
            long sizeInBytes = FileSize.parseSize(totalSize);
            long lastSizeInBytes = size != null ? FileSize.parseSize(size) : 0;

            double thresholdMultiplier = 2.5;
            boolean isSizeWithinThreshold = sizeInBytes >= lastSizeInBytes / thresholdMultiplier &&
                    sizeInBytes <= lastSizeInBytes * thresholdMultiplier;
            boolean isValidSize = progress < 30 || isSizeWithinThreshold;

            if (isValidSize) {
                size = totalSize;
            }
        }

        // Check speed
        Matcher speedMatcher = DOWNLOAD_SPEED_REGEX.matcher(text);
        if (speedMatcher.find()) {
            speed = speedMatcher.group(1);
        }

        // Check ETA
        Matcher etaMatcher = DOWNLOAD_ETA_REGEX.matcher(text);
        if (etaMatcher.find()) {
            eta = etaMatcher.group(1);
        }

        return getDownloadProgress();
    }

    public CompletableFuture<DownloadProgress> download(Consumer<DownloadProgress> progressCallback) {
        List<String> args = Arrays.asList(url);
        return cmd.execute(args, text -> progressCallback.accept(onOutput(text)))
                .thenApply(unused -> {
                    hasEnded = true;
                    try {
                        File file = new File(mediaPath, filename);
                        long fileSize = Files.size(file.toPath());
                        this.size = FileSize.formatSize(fileSize);
                    } catch (Exception e) {
                        System.err.println("Error getting file size: " + e.getMessage());
                    }
                    return getDownloadProgress();
                });
    }
}
