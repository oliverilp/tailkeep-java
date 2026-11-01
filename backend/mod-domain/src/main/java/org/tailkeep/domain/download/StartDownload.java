package org.tailkeep.domain.download;

public interface StartDownload {
    /**
     * Step 1 of the pipeline: checks the URL, saves a job for it and publishes a metadata request.
     *
     * @throws InvalidUrlException when the URL is not acceptable or does not answer
     */
    void execute(String url);
}
