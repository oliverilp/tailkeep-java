package org.tailkeep.api.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.dto.DownloadProgressMessage;
import org.tailkeep.api.dto.MetadataRequestMessage;
import org.tailkeep.api.model.DownloadProgress;
import org.tailkeep.api.model.Job;
import org.tailkeep.api.repository.DownloadProgressRepository;
import org.tailkeep.api.repository.JobRepository;

@Service
public class DownloadService {
    private final KafkaTemplate<String, MetadataRequestMessage> kafkaTemplate;
    private final JobRepository jobRepository;
    private final DownloadProgressRepository downloadProgressRepository;

    public DownloadService(
            KafkaTemplate<String, MetadataRequestMessage> kafkaTemplate, 
            JobRepository jobRepository,
            DownloadProgressRepository downloadProgressRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.jobRepository = jobRepository;
        this.downloadProgressRepository = downloadProgressRepository;
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
    public DownloadProgress updateDownloadProgress(DownloadProgressMessage message) {
        DownloadProgress progress = downloadProgressRepository.findById(message.jobId())
            .orElseThrow(() -> new RuntimeException("Download progress not found for job: " + message.jobId()));

        // Update fields
        progress.setStatus(message.status());
        progress.setProgress(message.progress());
        progress.setSize(message.size());
        progress.setSpeed(message.speed());
        progress.setEta(message.eta());
        progress.setHasEnded(message.hasEnded());
        
        return downloadProgressRepository.save(progress);
    }

    @Transactional
    public DownloadProgress markDownloadComplete(DownloadProgressMessage message) {
        DownloadProgress progress = downloadProgressRepository.findById(message.jobId())
            .orElseThrow(() -> new RuntimeException("Download progress not found for job: " + message.jobId()));

        // Update completion fields
        progress.setProgress(100.0);
        progress.setStatus("done");
        progress.setSpeed("0B/s");
        progress.setEta("00:00");
        progress.setCompletedAt(LocalDateTime.now());
        progress.setHasEnded(true);

        return downloadProgressRepository.save(progress);
    }

    public List<DownloadProgress> getAllDownloadProgress() {
        return downloadProgressRepository.findAll(
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
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
