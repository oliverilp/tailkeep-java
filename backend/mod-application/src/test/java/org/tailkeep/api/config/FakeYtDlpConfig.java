package org.tailkeep.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.tailkeep.adapter.ytdlp.config.YtDlpProperties;
import org.tailkeep.worker.pipeline.FakeYtDlp;

@TestConfiguration(proxyBeanMethods = false)
public class FakeYtDlpConfig {

    @Bean
    @Primary
    public FakeYtDlp fakeYtDlp(YtDlpProperties properties) {
        // The adapter reads the downloaded file's size from the media path, so the fake writes the file there
        return new FakeYtDlp(properties.getPath());
    }
}
