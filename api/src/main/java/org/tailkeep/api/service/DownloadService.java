package org.tailkeep.api.service;

import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.dto.MetadataRequestMessage;

@Service
public class DownloadService {
    private final KafkaTemplate<String, MetadataRequestMessage> kafkaTemplate;

    public DownloadService(KafkaTemplate<String, MetadataRequestMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void processDownloadRequest(MetadataRequestMessage request) throws Exception {
        String jobId = UUID.randomUUID().toString();
        String cleanedUrl = cleanUrl(request.url());
        
        MetadataRequestMessage enrichedRequest = new MetadataRequestMessage(jobId, cleanedUrl);
        kafkaTemplate.send(KafkaTopicNames.METADATA_QUEUE, enrichedRequest);
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
