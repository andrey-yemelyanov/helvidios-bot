package org.helvidios.crawler.http;

import java.net.URI;
import java.util.Objects;

/**
 * Indicates a failure to download a web page from a url.
 */
public class FetchException extends Exception {
    private final URI url;

    public FetchException(URI url, Throwable causedBy){
        super(
            String.format("Unable to fetch web page from '%s'", 
            Objects.requireNonNull(url, "url must not be null")), 
            causedBy);
        this.url = url;
    }

    /**
     * URL from which page download failed
     * @return url
     */
    public URI url(){
        return url;
    }
}
