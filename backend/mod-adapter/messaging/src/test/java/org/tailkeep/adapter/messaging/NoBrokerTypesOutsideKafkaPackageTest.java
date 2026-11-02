package org.tailkeep.adapter.messaging;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** Only the {@code kafka} package may know the broker, so another broker is a new package and config. */
class NoBrokerTypesOutsideKafkaPackageTest {
    @Test
    void onlyTheKafkaPackageImportsKafkaTypes() throws IOException {
        Path source = Path.of("src/main/java/org/tailkeep/adapter/messaging");
        Path kafkaPackage = source.resolve("kafka");

        List<String> violations;
        try (Stream<Path> files = Files.walk(source)) {
            violations = files
                    .filter(file -> file.toString().endsWith(".java"))
                    .filter(file -> !file.startsWith(kafkaPackage))
                    .filter(NoBrokerTypesOutsideKafkaPackageTest::importsKafka)
                    .map(Path::toString)
                    .toList();
        }

        assertThat(violations).isEmpty();
    }

    private static boolean importsKafka(Path file) {
        try {
            return Files.readAllLines(file).stream().anyMatch(line ->
                    line.startsWith("import org.apache.kafka") || line.startsWith("import org.springframework.kafka"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
