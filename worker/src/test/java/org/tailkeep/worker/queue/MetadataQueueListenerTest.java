package org.tailkeep.worker.queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.tailkeep.worker.config.KafkaTopicNames;
import org.tailkeep.worker.metadata.*;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetadataQueueListenerTest {

     @Mock
     private MetadataFetcher metadataFetcher;

     @Mock
     private KafkaTemplate<String, MetadataResultMessage> kafkaTemplate;

     @Captor
     private ArgumentCaptor<MetadataResultMessage> resultMessageCaptor;

     private MetadataQueueListener listener;

     @BeforeEach
     void setUp() {
         listener = new MetadataQueueListener(metadataFetcher, kafkaTemplate);
     }

     @Test
     void onMetadataJob_ShouldFetchMetadataAndSendResult() {
         // Given
         String jobId = "job123";
         String url = "https://youtube.com/watch?v=test";
         MetadataRequestMessage request = new MetadataRequestMessage(jobId, url);
        
         Metadata metadata = new Metadata(
             "test-id",
             "https://youtube.com/watch?v=test",
             "Test Video",
             "Test Channel",
             "UC123456789",
             "https://youtube.com/c/testchannel",
             "10:00",
             600.0,
             "https://i.ytimg.com/vi/test/maxres.jpg",
             "This is a test video description",
             1000L,
             100L,
             "test.mp4"
         );

         when(metadataFetcher.fetch(url))
             .thenReturn(CompletableFuture.completedFuture(metadata));

         // When
         listener.onMetadataJob(request);

         // Then
         verify(metadataFetcher).fetch(url);
         verify(kafkaTemplate).send(
             eq(KafkaTopicNames.METADATA_RESULTS),
             resultMessageCaptor.capture()
         );

         MetadataResultMessage capturedMessage = resultMessageCaptor.getValue();
         assertThat(capturedMessage.jobId()).isEqualTo(jobId);
         assertThat(capturedMessage.metadata()).isEqualTo(metadata);
     }
} 
