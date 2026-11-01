package org.tailkeep.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.tailkeep.worker.command.ProcessFactory;
import org.tailkeep.worker.pipeline.FakeYtDlp;

@TestConfiguration(proxyBeanMethods = false)
public class FakeYtDlpConfig {

    @Bean
    @Primary
    public FakeYtDlp fakeYtDlp() {
        // The worker reads the downloaded file's size from MEDIA_PATH, which the build sets for tests
        return new FakeYtDlp(System.getenv("MEDIA_PATH"));
    }
}
