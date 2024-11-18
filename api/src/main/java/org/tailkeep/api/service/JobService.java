package org.tailkeep.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.api.model.Job;
import org.tailkeep.api.model.Video;
import org.tailkeep.api.repository.JobRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;

    @Transactional
    public Job updateJobMetadata(String jobId, Video video) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        
        job.setVideo(video);
        return jobRepository.save(job);
    }
}
