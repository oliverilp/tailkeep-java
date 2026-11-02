package org.tailkeep.domain.download.usecase;

import org.tailkeep.domain.download.model.DownloadUrl;
import org.tailkeep.domain.download.model.InvalidUrlException;
import org.tailkeep.domain.download.model.Job;
import org.tailkeep.domain.download.model.MetadataRequest;
import org.tailkeep.domain.download.port.ProbeUrl;
import org.tailkeep.domain.download.port.PublishMetadataRequest;
import org.tailkeep.domain.download.port.SaveJob;
import org.tailkeep.domain.shared.UseCase;

@UseCase
public class StartDownloadImpl implements StartDownload {
    private final ProbeUrl probeUrl;
    private final SaveJob saveJob;
    private final PublishMetadataRequest publishMetadataRequest;

    public StartDownloadImpl(ProbeUrl probeUrl, SaveJob saveJob, PublishMetadataRequest publishMetadataRequest) {
        this.probeUrl = probeUrl;
        this.saveJob = saveJob;
        this.publishMetadataRequest = publishMetadataRequest;
    }

    @Override
    public void execute(String url) {
        DownloadUrl downloadUrl = DownloadUrl.parse(url);
        checkAnswers(downloadUrl);

        // The job keeps the URL as it was entered, the metadata request gets the cleaned one
        Job job = saveJob.save(Job.builder().inputUrl(downloadUrl.value()).build());
        publishMetadataRequest.publish(new MetadataRequest(job.id(), downloadUrl.cleaned()));
    }

    private void checkAnswers(DownloadUrl url) {
        int status;
        try {
            status = probeUrl.headStatus(url.value());
        } catch (ProbeUrl.UrlProbeException e) {
            throw new InvalidUrlException("Failed to validate URL");
        }
        if (status >= 400) {
            throw new InvalidUrlException("URL returned error status: " + status);
        }
    }
}
