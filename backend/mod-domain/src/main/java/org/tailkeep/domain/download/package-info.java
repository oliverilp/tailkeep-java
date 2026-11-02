/**
 * Downloading a video. The pipeline has six steps. Each runs from a message handler (step 1 from the REST
 * endpoint) that calls one use case, and each use case hands over to the next through a port:
 *
 * <ol>
 *   <li>{@link org.tailkeep.domain.download.usecase.StartDownload} saves a
 *       {@link org.tailkeep.domain.download.model.Job} and publishes with
 *       {@link org.tailkeep.domain.download.port.PublishMetadataRequest}.</li>
 *   <li>{@link org.tailkeep.domain.download.usecase.FetchMetadata} asks the
 *       {@link org.tailkeep.domain.download.port.VideoMetadataProvider} and publishes with
 *       {@link org.tailkeep.domain.download.port.PublishMetadataResult}.</li>
 *   <li>{@link org.tailkeep.domain.download.usecase.RecordMetadata} saves the channel, video and job and
 *       publishes with {@link org.tailkeep.domain.download.port.PublishDownloadRequest}.</li>
 *   <li>{@link org.tailkeep.domain.download.usecase.RunDownload} calls the
 *       {@link org.tailkeep.domain.download.port.VideoDownloader} and publishes with
 *       {@link org.tailkeep.domain.download.port.PublishDownloadProgress}.</li>
 *   <li>{@link org.tailkeep.domain.download.usecase.RecordDownloadProgress} saves the progress on the job.</li>
 *   <li>The same use case then calls {@link org.tailkeep.domain.download.port.BroadcastDownloadProgress}, which
 *       reaches every instance. Each one runs {@link org.tailkeep.domain.download.usecase.PushDownloadProgress},
 *       which sends the download to its own clients with
 *       {@link org.tailkeep.domain.download.port.SendDownloadToClients}.</li>
 * </ol>
 *
 * Every step can be delivered more than once. Steps 2, 3 and 5 give the same result, and step 4 skips a job whose
 * download has ended.
 *
 * The rest are read and delete use cases for the REST endpoints. Technology details stay in the adapters.
 */
package org.tailkeep.domain.download;
