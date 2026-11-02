package org.tailkeep.adapter.ytdlp.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tailkeep.adapter.ytdlp.config.YtDlpProperties;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YtDlpRunnerTest {
    private static final List<String> EXPECTED_COMMAND = List.of("yt-dlp", "test", "command");

    @Mock
    private ProcessFactory processFactory;
    @Mock
    private Process process;

    private YtDlpRunner runner;
    private File mediaPath;

    @BeforeEach
    void setUp() throws Exception {
        YtDlpProperties properties = new YtDlpProperties();
        runner = new YtDlpRunner(processFactory, properties);
        mediaPath = new File(properties.getPath());

        when(processFactory.createProcess(EXPECTED_COMMAND, mediaPath)).thenReturn(process);
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    }

    @Test
    void run_PassesEachStdoutLineToTheCallback() throws Exception {
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream("first\nsecond\n".getBytes()));
        when(process.waitFor()).thenReturn(0);
        List<String> lines = new ArrayList<>();

        runner.run(List.of("test", "command"), lines::add);

        assertThat(lines).containsExactly("first", "second");
    }

    @Test
    void run_WhenProcessFails_Throws() throws Exception {
        when(process.waitFor()).thenReturn(1);

        assertThatThrownBy(() -> runner.run(List.of("test", "command"), line -> {}))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Process exited with code 1");
    }

    @Test
    void run_WhenAnotherCommandIsRunning_Throws() throws Exception {
        CountDownLatch running = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(process.waitFor()).thenAnswer(inv -> {
            running.countDown();
            release.await();
            return 0;
        });

        CompletableFuture<Void> first = CompletableFuture.runAsync(
                () -> runner.run(List.of("test", "command"), line -> {}));
        running.await();

        assertThatThrownBy(() -> runner.run(List.of("test", "command"), line -> {}))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Another command is already running");

        release.countDown();
        first.join();
    }

    @Test
    void run_DestroysAProcessThatIsStillAlive() throws Exception {
        when(process.waitFor()).thenReturn(1);
        when(process.isAlive()).thenReturn(true);

        assertThatThrownBy(() -> runner.run(List.of("test", "command"), line -> {}))
                .isInstanceOf(RuntimeException.class);

        verify(process).destroy();
    }
}
