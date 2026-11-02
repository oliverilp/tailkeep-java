package org.tailkeep.domain.download.usecase;

import org.tailkeep.domain.download.model.MetadataRequest;
import org.tailkeep.domain.download.model.MetadataResult;
import org.tailkeep.domain.download.model.VideoMetadata;
import org.tailkeep.domain.download.port.PublishMetadataResult;
import org.tailkeep.domain.download.port.VideoMetadataProvider;
import org.tailkeep.domain.shared.UseCase;

@UseCase
public class FetchMetadataImpl implements FetchMetadata {
    private final VideoMetadataProvider videoMetadataProvider;
    private final PublishMetadataResult publishMetadataResult;

    public FetchMetadataImpl(VideoMetadataProvider videoMetadataProvider, PublishMetadataResult publishMetadataResult) {
        this.videoMetadataProvider = videoMetadataProvider;
        this.publishMetadataResult = publishMetadataResult;
    }

    @Override
    public void execute(MetadataRequest request) {
        VideoMetadata metadata = videoMetadataProvider.fetch(request.url());
        publishMetadataResult.publish(new MetadataResult(request.jobId(), metadata));
    }
}
