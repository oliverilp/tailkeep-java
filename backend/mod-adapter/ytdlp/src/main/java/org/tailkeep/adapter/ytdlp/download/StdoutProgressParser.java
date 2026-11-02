package org.tailkeep.adapter.ytdlp.download;

import org.tailkeep.domain.download.model.ProgressUpdate;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns the lines yt-dlp prints while downloading into progress updates. One instance follows one download.
 * This is the only class that knows what yt-dlp's progress output looks like.
 */
public class StdoutProgressParser {
    private static final Pattern CATEGORY_REGEX = Pattern.compile("^(?:\\[)(.*?)(?:\\])");
    private static final Pattern PROGRESS_REGEX = Pattern.compile("(\\d+\\.?\\d*)%");
    private static final Pattern SIZE_REGEX = Pattern.compile("of\\s+~?\\s+(\\d+\\.\\d+\\w{1,3})");
    private static final Pattern SPEED_REGEX = Pattern.compile("at\\s+(\\d+\\.\\d+\\w{1,3}/s)");
    private static final Pattern ETA_REGEX = Pattern.compile("ETA\\s+(\\d+:\\d+(?::\\d+)?)");

    /** Fragmented downloads report a wrong total now and then, so a size that jumps further is ignored. */
    private static final double SIZE_JUMP_LIMIT = 2.5;
    private static final double PROGRESS_WHERE_SIZE_IS_TRUSTED = 30;

    private final String youtubeId;
    private final UUID jobId;

    private String status = null;
    private double progress = 0;
    private String size = null;
    private String speed = null;
    private String eta = null;

    public StdoutProgressParser(String youtubeId, UUID jobId) {
        this.youtubeId = youtubeId;
        this.jobId = jobId;
    }

    public ProgressUpdate onLine(String line) {
        Matcher statusMatcher = CATEGORY_REGEX.matcher(line);
        if (statusMatcher.find()) {
            status = statusMatcher.group(1);
        }

        Matcher progressMatcher = PROGRESS_REGEX.matcher(line);
        if (progressMatcher.find()) {
            progress = Double.parseDouble(progressMatcher.group(1));
        }

        Matcher sizeMatcher = SIZE_REGEX.matcher(line);
        if (sizeMatcher.find()) {
            String totalSize = sizeMatcher.group(1);
            if (isPlausible(totalSize)) {
                size = totalSize;
            }
        }

        Matcher speedMatcher = SPEED_REGEX.matcher(line);
        if (speedMatcher.find()) {
            speed = speedMatcher.group(1);
        }

        Matcher etaMatcher = ETA_REGEX.matcher(line);
        if (etaMatcher.find()) {
            eta = etaMatcher.group(1);
        }

        return update(false);
    }

    /** The last update, sent when yt-dlp has exited. {@code finalSize} replaces the size when it is not null. */
    public ProgressUpdate finish(String finalSize) {
        if (finalSize != null) {
            size = finalSize;
        }
        return update(true);
    }

    private boolean isPlausible(String totalSize) {
        long sizeInBytes = FileSize.parseSize(totalSize);
        long lastSizeInBytes = size != null ? FileSize.parseSize(size) : 0;
        boolean isWithinLimit = sizeInBytes >= lastSizeInBytes / SIZE_JUMP_LIMIT
                && sizeInBytes <= lastSizeInBytes * SIZE_JUMP_LIMIT;
        return progress < PROGRESS_WHERE_SIZE_IS_TRUSTED || isWithinLimit;
    }

    private ProgressUpdate update(boolean hasEnded) {
        return new ProgressUpdate(youtubeId, jobId, status, hasEnded, progress, size, speed, eta);
    }
}
