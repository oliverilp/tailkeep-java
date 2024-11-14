package org.tailkeep.worker;

// import java.util.Arrays;
// import java.util.List;

// import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.annotation.Bean;
// import org.tailkeep.worker.command.CommandExecutor;
// import org.tailkeep.worker.metadata.Metadata;
// import org.tailkeep.worker.metadata.MetadataFetcher;
// import org.tailkeep.worker.queue.KafkaCallback;

@SpringBootApplication
public class TailkeepWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TailkeepWorkerApplication.class, args);
	}

	// @Bean
	// CommandLineRunner commandLineRunner(KafkaTemplate<String, String>
	// kafkaTemplate) {
	// return args -> {
	// for (int i = 0; i < 100; i++) {
	// kafkaTemplate.send("tailkeep", "hello kafka " + i);
	// }
	// };
	// }

	// @Bean
	// CommandLineRunner runOnStartup(MetadataFetcher metadataFetcher) {
	// System.out.println("\n\n\nRunning metadata fetch test on startup");
	// return args -> {
	// String url = "https://www.youtube.com/watch?v=R7t7zca8SyM";
	// try {
	// Metadata metadata = metadataFetcher.fetch(url)
	// .exceptionally(throwable -> {
	// System.err.println("Error fetching metadata: " + throwable.getMessage());
	// throwable.printStackTrace();
	// return null;
	// })
	// .join(); // Blocks until complete

	// if (metadata != null) {
	// System.out.println("\nMetadata fetch completed successfully:");
	// System.out.println("----------------------------------------");
	// System.out.println("Title: " + metadata.title());
	// System.out.println("Channel: " + metadata.uploader());
	// System.out.println("Duration: " + metadata.durationString());
	// System.out.println("Views: " + metadata.viewCount());
	// System.out.println("Video ID: " + metadata.youtubeId());
	// System.out.println("Filename: " + metadata.filename());
	// System.out.println("----------------------------------------\n");
	// }
	// } catch (Exception e) {
	// System.err.println("Fatal error during metadata fetch:");
	// e.printStackTrace();
	// }
	// };
	// }
}
