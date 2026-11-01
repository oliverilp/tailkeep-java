package org.tailkeep.worker.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.tailkeep.adapter.ytdlp.config.YtDlpProperties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = YtDlpPropertiesTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "MEDIA_PATH=/videos")
@ActiveProfiles("prod")
class YtDlpPropertiesTest {
    @Autowired
    private YtDlpProperties properties;

    @Test
    void prodProfile_ShouldUseMediaPathVariable() {
        assertThat(properties.getPath()).isEqualTo("/videos");
    }

    @EnableConfigurationProperties
    @Import(YtDlpProperties.class)
    static class TestConfig {
    }
}
