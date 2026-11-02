package org.tailkeep.adapter.database.download;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.adapter.database.shared.PageMapper;
import org.tailkeep.adapter.database.video.VideoEntity;
import org.tailkeep.adapter.database.video.VideoJpaRepository;
import org.tailkeep.domain.download.model.DownloadFilter;
import org.tailkeep.domain.download.model.DownloadProgress;
import org.tailkeep.domain.download.model.Job;
import org.tailkeep.domain.download.model.QueueInfo;
import org.tailkeep.domain.download.port.CountDownloads;
import org.tailkeep.domain.download.port.FindDownload;
import org.tailkeep.domain.download.port.FindJob;
import org.tailkeep.domain.download.port.SaveJob;
import org.tailkeep.domain.shared.PageQuery;
import org.tailkeep.domain.shared.PageResult;
import org.tailkeep.domain.shared.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class DownloadDatabaseAdapter implements FindJob, SaveJob, FindDownload, CountDownloads {
    private final JobRepository jobs;
    private final DownloadProgressRepository downloads;
    private final VideoJpaRepository videos;

    public DownloadDatabaseAdapter(
            JobRepository jobs, DownloadProgressRepository downloads, VideoJpaRepository videos) {
        this.jobs = jobs;
        this.downloads = downloads;
        this.videos = videos;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Job> byId(UUID id) {
        return jobs.findById(id).map(JobEntityMapper::toDomain);
    }

    @Override
    public Job save(Job job) {
        JobEntity entity = job.id() == null
                ? new JobEntity()
                : jobs.findById(job.id()).orElseThrow(() -> new ResourceNotFoundException("Job", job.id().toString()));
        if (job.id() == null) {
            // The progress shares the ID of the job, so the job needs its ID first
            JobEntityMapper.copyTo(job.toBuilder().progress(null).build(), entity, videoOf(job));
            entity = jobs.saveAndFlush(entity);
        }
        JobEntityMapper.copyTo(job, entity, videoOf(job));
        return JobEntityMapper.toDomain(jobs.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DownloadProgress> byJobId(UUID jobId) {
        return downloads.findById(jobId).map(JobEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DownloadProgress> newestFirst() {
        return downloads.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(JobEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<DownloadProgress> notDeleted(DownloadFilter filter, PageQuery query) {
        var pageable = PageMapper.toPageable(query);
        Page<DownloadProgressEntity> page = switch (filter) {
            case ACTIVE -> downloads.findByHasEndedFalseAndDeletedAtIsNull(pageable);
            case DONE -> downloads.findByHasEndedTrueAndDeletedAtIsNull(pageable);
            case ALL -> downloads.findByDeletedAtIsNull(pageable);
        };
        return PageMapper.toPageResult(page, JobEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public QueueInfo queueInfo() {
        return new QueueInfo(
                jobs.countQueuedJobs(),
                downloads.countByHasEndedFalseAndDeletedAtIsNull(),
                downloads.countByHasEndedTrueAndDeletedAtIsNull());
    }

    private VideoEntity videoOf(Job job) {
        return job.videoId() == null ? null : videos.getReferenceById(job.videoId());
    }
}
