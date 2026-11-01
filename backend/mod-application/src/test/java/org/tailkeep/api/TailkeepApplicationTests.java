package org.tailkeep.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.tailkeep.api.config.TestContainersConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
/**
 * Starts the application the way production does, without bean overriding, so two beans with the same name
 * fail here.
 */
class TailkeepApplicationTests {

    @Test
    void contextLoads() {
    }

}
