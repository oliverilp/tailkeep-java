package org.tailkeep.api.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.dto.DownloadProgressDto;
import org.tailkeep.api.mapper.EntityMapper;
import org.tailkeep.api.message.DownloadProgressMessage;
import org.tailkeep.api.message.MetadataRequestMessage;
import org.tailkeep.api.model.DownloadProgress;
import org.tailkeep.api.model.Job;
import org.tailkeep.api.repository.DownloadProgressRepository;
import org.tailkeep.api.repository.JobRepository;

@Service
public class DownloadService {
    private final KafkaTemplate<String, MetadataRequestMessage> kafkaTemplate;
    private final JobRepository jobRepository;
    private final DownloadProgressRepository downloadProgressRepository;
    private final EntityMapper mapper;

    public DownloadService(
            KafkaTemplate<String, MetadataRequestMessage> kafkaTemplate, 
            JobRepository jobRepository,
            DownloadProgressRepository downloadProgressRepository,
            EntityMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.jobRepository = jobRepository;
        this.downloadProgressRepository = downloadProgressRepository;
        this.mapper = mapper;
    }
    
    @Transactional
    public void processDownloadRequest(MetadataRequestMessage request) throws Exception {
        String cleanedUrl = cleanUrl(request.url());
        
        Job job = new Job();
        job.setInputUrl(request.url());
        job = jobRepository.save(job);
        
        MetadataRequestMessage enrichedRequest = new MetadataRequestMessage(job.getId(), cleanedUrl);
        kafkaTemplate.send(KafkaTopicNames.METADATA_QUEUE, enrichedRequest);
    }

    @Transactional
    public DownloadProgressDto upsertDownloadProgress(DownloadProgressMessage message) {
        Job job = jobRepository.findById(message.jobId())
            .orElseThrow(() -> new RuntimeException("Job not found: " + message.jobId()));

        DownloadProgress progress = job.getDownloadProgress();
        if (progress == null) {
            progress = new DownloadProgress();
            progress.setId(job.getId());
            progress.setJob(job);
            progress.setVideo(job.getVideo());
            job.setDownloadProgress(progress);
        }

        // Update fields
        progress.setStatus(message.status());
        progress.setProgress(message.progress());
        progress.setSize(message.size());
        progress.setSpeed(message.speed());
        progress.setEta(message.eta());
        progress.setHasEnded(message.hasEnded());
        
        // Save the job which will cascade to download_progress
        return mapper.toDto(jobRepository.save(job).getDownloadProgress());
    }

    @Transactional
    public DownloadProgressDto markDownloadComplete(DownloadProgressMessage message) {
        DownloadProgress progress = downloadProgressRepository.findById(message.jobId())
            .orElseThrow(() -> new RuntimeException("Download progress not found for job: " + message.jobId()));
        
        // Update completion fields
        progress.setStatus("done");
        progress.setProgress(100.0);
        progress.setSize(message.size());
        progress.setSpeed("0B/s");
        progress.setEta("00:00");
        progress.setCompletedAt(LocalDateTime.now());
        progress.setHasEnded(true);

        return mapper.toDto(downloadProgressRepository.save(progress));
    }

    public List<DownloadProgressDto> getAllDownloadProgress() {
        return downloadProgressRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
            .stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    public DownloadProgressDto getDownloadProgressById(String id) {
        return downloadProgressRepository.findById(id)
            .map(mapper::toDto)
            .orElseThrow(() -> new RuntimeException("Download progress not found: " + id));
    }

    private String cleanUrl(String url) throws Exception {
        URI uri = new URI(url);
        
        // If it's YouTube and has query params, keep only the 'v' parameter
        if (uri.getHost() != null && uri.getHost().contains("youtube.com") && uri.getQuery() != null) {
            String videoId = Arrays.stream(uri.getQuery().split("&"))
                .filter(param -> param.startsWith("v="))
                .map(param -> param.substring(2))
                .findFirst()
                .orElse(null);

            if (videoId != null) {
                return new URI(uri.getScheme(), 
                             uri.getAuthority(), 
                             uri.getPath(), 
                             "v=" + videoId, 
                             null).toString();
            }
        }

        // For all other URLs, remove query parameters
        return new URI(uri.getScheme(), 
                      uri.getAuthority(), 
                      uri.getPath(), 
                      null, 
                      null).toString();
    }
}
