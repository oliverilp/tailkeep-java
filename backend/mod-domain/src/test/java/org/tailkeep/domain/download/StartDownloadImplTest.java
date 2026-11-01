package org.tailkeep.domain.download;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StartDownloadImplTest {
    private static final String URL = "https://www.youtube.com/watch?v=abc&list=xyz";

    private final FakeJobs jobs = new FakeJobs();
    private final List<MetadataRequest> published = new ArrayList<>();
    private final List<String> probed = new ArrayList<>();
    private int status = 200;

    private final StartDownloadImpl startDownload = new StartDownloadImpl(url -> {
        probed.add(url);
        return status;
    }, jobs, published::add);

    @Test
    void execute_ShouldSaveTheJobWithTheEnteredUrlAndRequestTheCleanedOne() {
        startDownload.execute(URL);

        assertThat(jobs.size()).isEqualTo(1);
        Job job = jobs.byId(published.get(0).jobId()).orElseThrow();
        assertThat(job.inputUrl()).isEqualTo(URL);
        assertThat(job.videoId()).isNull();
        assertThat(published).singleElement().satisfies(request ->
                assertThat(request.url()).isEqualTo("https://www.youtube.com/watch?v=abc"));
        assertThat(probed).containsExactly(URL);
    }

    @Test
    void execute_WithAnUnacceptableUrl_ShouldNeitherProbeNorSaveNorPublish() {
        assertThatThrownBy(() -> startDownload.execute("http://example.com"))
                .isInstanceOf(InvalidUrlException.class)
                .hasMessage("URL must use HTTPS protocol");

        assertThat(probed).isEmpty();
        assertThat(jobs.size()).isZero();
        assertThat(published).isEmpty();
    }

    @Test
    void execute_WhenTheUrlAnswersWithAnError_ShouldFailWithTheStatus() {
        status = 404;

        assertThatThrownBy(() -> startDownload.execute(URL))
                .isInstanceOf(InvalidUrlException.class)
                .hasMessage("URL returned error status: 404");

        assertThat(jobs.size()).isZero();
        assertThat(published).isEmpty();
    }

    @Test
    void execute_WhenTheUrlCannotBeReached_ShouldFailToValidate() {
        StartDownloadImpl unreachable = new StartDownloadImpl(url -> {
            throw new ProbeUrl.UrlProbeException("timeout", new RuntimeException());
        }, jobs, published::add);

        assertThatThrownBy(() -> unreachable.execute(URL))
                .isInstanceOf(InvalidUrlException.class)
                .hasMessage("Failed to validate URL");
        assertThat(published).isEmpty();
    }
}
