package org.tailkeep.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.tailkeep.api.config.TestContainersConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
// Its Kafka listeners share consumer groups with the other tests' context, so it must not keep the partitions
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
/**
 * Starts the application the way production does, without bean overriding, so two beans with the same name
 * fail here.
 */
class TailkeepApplicationTests {

    @Test
    void contextLoads() {
    }

}
