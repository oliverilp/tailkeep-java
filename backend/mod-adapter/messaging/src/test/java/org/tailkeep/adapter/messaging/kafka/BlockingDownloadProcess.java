package org.tailkeep.adapter.messaging.kafka;

/**
 * An app instance in its own JVM whose downloads never end. {@link KafkaRedeliveryTest} kills it mid-download.
 * It prints {@code READY} when it has its partitions and {@code STARTED <jobId>} when a download began.
 */
public final class BlockingDownloadProcess {
    private BlockingDownloadProcess() {
    }

    public static void main(String[] args) throws Exception {
        TestInstance instance = TestInstance.start(args[0], true, 0);
        while (!instance.isAssigned(false)) {
            Thread.sleep(200);
        }
        System.out.println("READY");
        System.out.flush();
        Thread.sleep(Long.MAX_VALUE);
    }
}
