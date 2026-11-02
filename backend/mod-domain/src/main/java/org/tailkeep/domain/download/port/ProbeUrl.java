package org.tailkeep.domain.download.port;

/** Outbound port that asks the server of a URL whether the URL exists. */
public interface ProbeUrl {
    /**
     * Sends a HEAD request and returns the HTTP status code.
     *
     * @throws UrlProbeException when the server cannot be reached
     */
    int headStatus(String url);

    class UrlProbeException extends RuntimeException {
        public UrlProbeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
