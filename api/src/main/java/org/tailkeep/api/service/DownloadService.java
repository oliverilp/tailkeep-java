package org.tailkeep.api.service;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
import org.tailkeep.api.dto.DownloadRequestDto;
import org.tailkeep.api.dto.DownloadsDashboardDto;
import org.tailkeep.api.dto.QueueInfoDto;
import org.tailkeep.api.exception.InvalidUrlException;
import org.tailkeep.api.exception.ResourceNotFoundException;
import org.tailkeep.api.mapper.EntityMapper;
import org.tailkeep.api.message.DownloadProgressMessage;
import org.tailkeep.api.message.MetadataRequestMessage;
import org.tailkeep.api.model.DownloadProgress;
import org.tailkeep.api.model.Job;
import org.tailkeep.api.repository.DownloadProgressRepository;
import org.tailkeep.api.repository.JobRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DownloadService {
    private final KafkaTemplate<String, MetadataRequestMessage> kafkaTemplate;
    private final JobRepository jobRepository;
    private final DownloadProgressRepository downloadProgressRepository;
    private final EntityMapper mapper;

    public void validateAndStartDownload(DownloadRequestDto request) {
        validateUrl(request.url());

        MetadataRequestMessage message = new MetadataRequestMessage(null, request.url());
        try {
            processDownloadRequest(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process download request", e);
        }
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
            .orElseThrow(() -> new ResourceNotFoundException("Job", message.jobId()));

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
            .orElseThrow(() -> new ResourceNotFoundException("Download progress", message.jobId()));
        
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
            .orElseThrow(() -> new ResourceNotFoundException("Download progress", id));
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

    private void validateUrl(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = URI.create(urlString).toURL();
            
            if (!url.getProtocol().equals("https")) {
                throw new InvalidUrlException("URL must use HTTPS protocol");
            }

            if (url.getHost().isEmpty()) {
                throw new InvalidUrlException("URL must have a valid host");
            }

            if (!url.getHost().contains(".")) {
                throw new InvalidUrlException("URL must have a valid domain");
            }

            // Try to actually connect to validate URL exists
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(1000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode >= 400) {
                throw new InvalidUrlException("URL returned error status: " + responseCode);
            }

        } catch (MalformedURLException e) {
            throw new InvalidUrlException("Invalid URL format");
        } catch (InvalidUrlException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidUrlException("Failed to validate URL");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public DownloadsDashboardDto getDownloadsDashboard() {
        QueueInfoDto queueInfo = new QueueInfoDto(
            jobRepository.countQueuedJobs(),
            downloadProgressRepository.countByHasEndedFalse(),
            downloadProgressRepository.countByHasEndedTrue()
        );

        List<DownloadProgressDto> downloads = getAllDownloadProgress();

        return new DownloadsDashboardDto(queueInfo, downloads);
    }
}
