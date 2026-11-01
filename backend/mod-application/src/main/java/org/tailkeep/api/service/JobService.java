package org.tailkeep.api.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.adapter.database.download.JobEntity;
import org.tailkeep.adapter.database.download.JobRepository;
import org.tailkeep.adapter.database.video.VideoJpaRepository;
import org.tailkeep.domain.shared.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

// Legacy: the job moves to the domain in task 03
@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final VideoJpaRepository videoRepository;

    @Transactional
    public JobEntity updateJobMetadata(UUID jobId, UUID videoId) {
        JobEntity job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job", jobId.toString()));
        
        job.setVideo(videoRepository.getReferenceById(videoId));
        return jobRepository.save(job);
    }
}
