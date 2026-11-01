package org.tailkeep.domain.download;

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
