package org.tailkeep.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.tailkeep.api.integration.util.KafkaTestHelper;
import org.tailkeep.api.integration.util.TestDataFactory;
import org.tailkeep.api.message.MetadataResultMessage;
import org.tailkeep.api.message.Metadata;
import org.tailkeep.api.model.Channel;
import org.tailkeep.api.model.Job;
import org.tailkeep.api.model.Video;
import org.tailkeep.api.repository.JobRepository;
import org.tailkeep.api.repository.VideoRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MetadataResultListenerTest extends BaseIntegrationTest {

    @Autowired
    private KafkaTestHelper kafkaTestHelper;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Test
    void whenMetadataResultReceived_shouldCreateVideoAndUpdateJob() {
        // Arrange
        Job job = testDataFactory.createTestJob(null, "https://youtube.com/watch?v=test123", 1);
        
        Metadata metadata = new Metadata(
            "test123",                                      // youtubeId
            "https://youtube.com/watch?v=test123",         // url
            "Test Video",                                  // title
            "Test Uploader",                              // uploader
            "UC123",                                      // channelId
            "https://youtube.com/channel/UC123",          // channelUrl
            "10:00",                                      // durationString
            600.0,                                        // duration
            "https://img.youtube.com/test123/default.jpg",// thumbnailUrl
            "Test video description",                     // description
            1000L,                                        // viewCount
            100L,                                         // commentCount
            "test123.mp4"                                // filename
        );

        MetadataResultMessage message = new MetadataResultMessage(job.getId(), metadata);

        // Act
        kafkaTestHelper.sendMetadataResult(message);

        // Assert
        kafkaTestHelper.waitUntil(
            () -> jobRepository.findById(job.getId())
                .map(Job::getVideo)
                .map(Video::getYoutubeId)
                .orElse(null),
            "test123"
        );

        Job updatedJob = jobRepository.findById(job.getId()).orElseThrow();
        Video video = updatedJob.getVideo();
        Channel channel = video.getChannel();

        assertThat(video)
            .isNotNull()
            .satisfies(v -> {
                assertThat(v.getTitle()).isEqualTo("Test Video");
                assertThat(v.getYoutubeId()).isEqualTo("test123");
                assertThat(v.getUrl()).isEqualTo("https://youtube.com/watch?v=test123");
                assertThat(v.getFilename()).isEqualTo("test123.mp4");
            });

        assertThat(channel)
            .isNotNull()
            .satisfies(c -> {
                assertThat(c.getName()).isEqualTo("Test Uploader");
                assertThat(c.getYoutubeId()).isEqualTo("UC123");
                assertThat(c.getChannelUrl()).isEqualTo("https://youtube.com/channel/UC123");
            });
    }
}
