package org.tailkeep.domain.download;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

/** The URL a user asked to download, once it is known to be an acceptable one. */
public final class DownloadUrl {
    private final String value;

    private DownloadUrl(String value) {
        this.value = value;
    }

    /**
     * Checks the form of the URL: HTTPS, with a host that has a domain. Whether it answers is checked
     * separately, through {@link ProbeUrl}.
     *
     * @throws InvalidUrlException when it is not acceptable
     */
    public static DownloadUrl parse(String value) {
        try {
            URL url = URI.create(value).toURL();

            if (!url.getProtocol().equals("https")) {
                throw new InvalidUrlException("URL must use HTTPS protocol");
            }
            if (url.getHost().isEmpty()) {
                throw new InvalidUrlException("URL must have a valid host");
            }
            if (!url.getHost().contains(".")) {
                throw new InvalidUrlException("URL must have a valid domain");
            }
            return new DownloadUrl(value);
        } catch (MalformedURLException e) {
            throw new InvalidUrlException("Invalid URL format");
        } catch (InvalidUrlException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidUrlException("Failed to validate URL");
        }
    }

    /** The URL as it was entered. */
    public String value() {
        return value;
    }

    /**
     * The URL without what only identifies the visit. A YouTube watch URL keeps its {@code v} parameter and
     * any other URL loses its whole query.
     */
    public String cleaned() {
        try {
            URI uri = new URI(value);

            if (uri.getHost() != null && uri.getHost().contains("youtube.com") && uri.getQuery() != null) {
                String videoId = Arrays.stream(uri.getQuery().split("&"))
                        .filter(param -> param.startsWith("v="))
                        .map(param -> param.substring(2))
                        .findFirst()
                        .orElse(null);

                if (videoId != null) {
                    return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), "v=" + videoId, null)
                            .toString();
                }
            }

            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("A parsed URL could not be cleaned: " + value, e);
        }
    }
}
