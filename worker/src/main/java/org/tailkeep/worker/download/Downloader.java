package org.tailkeep.worker.download;

import org.tailkeep.worker.command.CommandExecutor;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Consumer;

public class Downloader {
    private static final Pattern DOWNLOAD_REGEX = Pattern.compile(
            "\\[(.*?)\\]\\s+(\\d+\\.?\\d*%)\\s+of\\s+~?\\s+(\\d+\\.\\d+\\w{1,3})\\s+(?:in \\d+:\\d+(?::\\d+)?\\s+)?at\\s+(\\d+\\.\\d+\\w{1,3}/s)(?:\\s+ETA\\s+(?:(\\d+:\\d+(?::\\d+)?)|Unknown))?\\s+\\(frag (\\d+)/\\d+\\)");
    private static final Pattern CATEGORY_REGEX = Pattern.compile("^(?:\\[)(.*?)(?:\\])");

    private final CommandExecutor cmd;
    private final String videoId;
    private final long jobId;
    private final String url;
    private final String filename;
    private final String mediaPath;

    private String status = null;
    private double progress = 0;
    private String size = null;
    private String speed = null;
    private String eta = null;

    public Downloader(CommandExecutor cmd, String videoId, long jobId, String url, String filename) {
        this.cmd = cmd;
        this.videoId = videoId;
        this.jobId = jobId;
        this.url = url;
        this.filename = filename;
        this.mediaPath = System.getenv("MEDIA_PATH");
    }

    private DownloadProgress getDownloadProgress() {
        return new DownloadProgress(videoId, jobId, status, progress, size, speed, eta);
    }

    private DownloadProgress onOutput(String text) {
        System.out.println(text);

        Matcher downloadMatcher = DOWNLOAD_REGEX.matcher(text);
        Matcher statusMatcher = CATEGORY_REGEX.matcher(text);

        if (statusMatcher.find()) {
            status = statusMatcher.group(1);
        }

        if (downloadMatcher.find()) {
            String matchStatus = downloadMatcher.group(1);
            String progressStr = downloadMatcher.group(2);
            String totalSize = downloadMatcher.group(3);
            String matchSpeed = downloadMatcher.group(4);
            String matchEta = downloadMatcher.group(5);
            String frag = downloadMatcher.group(6);

            double percentage = Double.parseDouble(progressStr.replace("%", ""));

            long sizeInBytes = FileSize.parseSize(totalSize);
            long lastSizeInBytes = size != null ? FileSize.parseSize(size) : 0;

            double thresholdMultiplier = 2.5;
            boolean isSizeWithinThreshold = sizeInBytes >= lastSizeInBytes / thresholdMultiplier &&
                    sizeInBytes <= lastSizeInBytes * thresholdMultiplier;
            boolean isValidSize = progress < 30 || isSizeWithinThreshold;

            if (matchStatus != null) {
                status = matchStatus;
            }
            if (isValidSize) {
                size = totalSize;
            }
            if (matchSpeed != null) {
                speed = matchSpeed;
            }
            if (matchEta != null) {
                eta = matchEta;
            }

            if (percentage >= progress && Integer.parseInt(frag) > 0) {
                progress = percentage;
            }
        }

        return getDownloadProgress();
    }

    public CompletableFuture<DownloadProgress> download(Consumer<DownloadProgress> progressCallback) {
        List<String> args = Arrays.asList(url);
        return cmd.execute(args, text -> progressCallback.accept(onOutput(text)))
                .thenApply(unused -> {
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
