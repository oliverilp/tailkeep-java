package org.tailkeep.worker.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = MediaPropertiesTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "MEDIA_PATH=/videos")
@ActiveProfiles("prod")
class MediaPropertiesTest {
    @Autowired
    private MediaProperties mediaProperties;

    @Test
    void prodProfile_ShouldUseMediaPathVariable() {
        assertThat(mediaProperties.getPath()).isEqualTo("/videos");
    }

    @EnableConfigurationProperties
    @Import(MediaProperties.class)
    static class TestConfig {
    }
}
