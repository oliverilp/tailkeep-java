package org.tailkeep.domain.download.model;

import lombok.Builder;
import org.tailkeep.domain.shared.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A request to download one URL, and the aggregate root of its {@link DownloadProgress}. {@code id} is null
 * until the job is saved, {@code videoId} until the metadata is recorded and {@code progress} until the
 * first progress arrives. The video is referred to by ID.
 */
@Builder(toBuilder = true)
public record Job(
        UUID id,
        String inputUrl,
        UUID videoId,
        DownloadProgress progress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    private static final String DONE = "done";

    /** The metadata of the job is known, so it now has a video. */
    public Job withVideo(UUID videoId) {
        return toBuilder().videoId(videoId).build();
    }

    /** Takes over what the downloader reports. The first report starts the progress. */
    public Job reportProgress(ProgressUpdate update) {
        DownloadProgress current = progress;
        if (current == null) {
            if (videoId == null) {
                throw new IllegalStateException("Job " + id + " has no video yet, so it cannot have progress");
            }
            current = DownloadProgress.builder().jobId(id).videoId(videoId).build();
        }
        return toBuilder().progress(current.toBuilder()
                .status(update.status())
                .progress(update.progress())
                .size(update.size())
                .speed(update.speed())
                .eta(update.eta())
                .hasEnded(update.hasEnded())
                .build()).build();
    }

    /**
     * The download finished with this size. It needs progress, because that is what is completed. Completing a
     * finished job again keeps the time it finished, so a message delivered twice changes nothing.
     */
    public Job complete(String size, LocalDateTime now) {
        DownloadProgress current = requireProgress();
        LocalDateTime completedAt = current.completedAt() != null ? current.completedAt() : now;
        return withProgress(current.toBuilder()
                .status(DONE)
                .progress(100.0)
                .size(size)
                .speed("0B/s")
                .eta("00:00")
                .hasEnded(true)
                .completedAt(completedAt)
                .build());
    }

    /** The download has ended or was hidden from the dashboard, so there is nothing left to download. */
    public boolean needsNoDownload() {
        return progress != null && (progress.hasEnded() || progress.isDeleted());
    }

    /** Hides the download from the dashboard. The row is kept. */
    public Job softDelete(LocalDateTime now) {
        return withProgress(requireProgress().toBuilder().deletedAt(now).build());
    }

    private Job withProgress(DownloadProgress progress) {
        return toBuilder().progress(progress).build();
    }

    private DownloadProgress requireProgress() {
        if (progress == null) {
            throw new ResourceNotFoundException("Download progress", String.valueOf(id));
        }
        return progress;
    }
}
