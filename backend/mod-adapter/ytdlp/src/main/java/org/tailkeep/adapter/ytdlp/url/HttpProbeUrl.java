package org.tailkeep.adapter.ytdlp.url;

import org.springframework.stereotype.Component;
import org.tailkeep.domain.download.port.ProbeUrl;

import java.net.HttpURLConnection;
import java.net.URI;

/** Asks the server of a URL with a HEAD request. */
@Component
public class HttpProbeUrl implements ProbeUrl {
    private static final int CONNECT_TIMEOUT_MS = 1000;

    @Override
    public int headStatus(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.connect();
            return connection.getResponseCode();
        } catch (Exception e) {
            throw new UrlProbeException("Could not reach " + url, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
