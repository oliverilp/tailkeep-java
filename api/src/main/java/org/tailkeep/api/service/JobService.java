package org.tailkeep.api.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.api.model.Job;
import org.tailkeep.api.model.Video;
import org.tailkeep.api.repository.JobRepository;
import org.tailkeep.api.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;

    @Transactional
    public Job updateJobMetadata(UUID jobId, Video video) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job", jobId.toString()));
        
        job.setVideo(video);
        return jobRepository.save(job);
    }
}
