package org.tailkeep.domain.download.usecase;

import org.tailkeep.domain.download.model.DownloadFilter;
import org.tailkeep.domain.download.model.DownloadProgress;
import org.tailkeep.domain.download.model.Job;
import org.tailkeep.domain.download.model.ProgressUpdate;
import org.tailkeep.domain.download.model.QueueInfo;
import org.tailkeep.domain.download.port.CountDownloads;
import org.tailkeep.domain.download.port.FindDownload;
import org.tailkeep.domain.download.port.FindJob;
import org.tailkeep.domain.download.port.SaveJob;
import org.tailkeep.domain.shared.PageQuery;
import org.tailkeep.domain.shared.PageResult;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory stand-in for the job and download ports. Every new job and progress is created a minute later. */
public class FakeJobs implements FindJob, SaveJob, FindDownload, CountDownloads {
    private static final LocalDateTime START = LocalDateTime.of(2025, 1, 1, 12, 0);

    private final Map<UUID, Job> jobs = new LinkedHashMap<>();
    private int created;

    @Override
    public Optional<Job> byId(UUID id) {
        return Optional.ofNullable(jobs.get(id));
    }

    @Override
    public Job save(Job job) {
        Job existing = job.id() == null ? null : jobs.get(job.id());
        UUID id = job.id() == null ? UUID.randomUUID() : job.id();
        LocalDateTime createdAt = existing != null ? existing.createdAt() : tick();

        DownloadProgress progress = job.progress();
        if (progress != null) {
            boolean isNew = existing == null || existing.progress() == null;
            progress = progress.toBuilder()
                    .jobId(id)
                    .createdAt(isNew ? tick() : existing.progress().createdAt())
                    .build();
        }

        Job saved = job.toBuilder().id(id).createdAt(createdAt).progress(progress).build();
        jobs.put(id, saved);
        return saved;
    }

    @Override
    public Optional<DownloadProgress> byJobId(UUID jobId) {
        return byId(jobId).map(Job::progress);
    }

    @Override
    public List<DownloadProgress> newestFirst() {
        return downloads().stream().sorted(newest()).toList();
    }

    @Override
    public PageResult<DownloadProgress> notDeleted(DownloadFilter filter, PageQuery query) {
        List<DownloadProgress> matching = downloads().stream()
                .filter(download -> !download.isDeleted())
                .filter(download -> switch (filter) {
                    case ALL -> true;
                    case ACTIVE -> !download.hasEnded();
                    case DONE -> download.hasEnded();
                })
                .sorted(newest())
                .toList();

        int from = Math.min((query.page() - 1) * query.size(), matching.size());
        int to = Math.min(from + query.size(), matching.size());
        return new PageResult<>(matching.subList(from, to), query.page(), query.size(), matching.size());
    }

    @Override
    public QueueInfo queueInfo() {
        List<DownloadProgress> visible = downloads().stream().filter(download -> !download.isDeleted()).toList();
        long jobsWithoutProgress = jobs.values().stream().filter(job -> job.progress() == null).count();
        return new QueueInfo(
                jobsWithoutProgress,
                visible.stream().filter(download -> !download.hasEnded()).count(),
                visible.stream().filter(DownloadProgress::hasEnded).count());
    }

    /** A job that has a video and has just started downloading. */
    public Job addDownloading(UUID videoId) {
        Job job = save(Job.builder().inputUrl("https://www.youtube.com/watch?v=x").videoId(videoId).build());
        return save(job.reportProgress(new ProgressUpdate("x", job.id(), "download", false, 10, "1MiB", "1MiB/s", "00:10")));
    }

    public int size() {
        return jobs.size();
    }

    private List<DownloadProgress> downloads() {
        return jobs.values().stream().map(Job::progress).filter(progress -> progress != null).toList();
    }

    private static Comparator<DownloadProgress> newest() {
        return Comparator.comparing(DownloadProgress::createdAt).reversed();
    }

    private LocalDateTime tick() {
        return START.plusMinutes(created++);
    }
}
