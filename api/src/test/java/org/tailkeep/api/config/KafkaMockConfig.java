package org.tailkeep.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.mockito.Mockito;
import org.tailkeep.api.message.DownloadRequestMessage;
import org.tailkeep.api.message.MetadataRequestMessage;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.tailkeep.api.listener.MetadataResultListener;
import org.tailkeep.api.listener.DownloadProgressListener;

@TestConfiguration
public class KafkaMockConfig {

    @MockBean
    private MetadataResultListener metadataResultListener;

    @MockBean
    private DownloadProgressListener downloadProgressListener;

    @Bean
    @Primary
    public KafkaTemplate<String, MetadataRequestMessage> metadataRequestKafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, DownloadRequestMessage> downloadRequestKafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }
} 
