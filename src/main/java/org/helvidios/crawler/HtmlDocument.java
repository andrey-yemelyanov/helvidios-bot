package org.helvidios.crawler;

import java.net.URI;
import java.util.List;

/**
 * Represents a generic HTML page with content.
 */
public interface HtmlDocument {
    
    /**
     * Returns raw HTML content of this page
     * @return HTML content
     */
    public String content();

    /**
     * Returns url from which this page was fetched
     * @return URL
     */
    public URI url();

    /**
     * Returns unique id assigned to this page
     * @return ID
     */
    public long docId();

    /**
     * Returns a list of URLs on this page
     * @return URLs on this page
     */
    public List<URI> urls();

    /**
     * Creates an {@link HtmlDocument}.
     * @param id document id
     * @param url URL
     * @param content raw HTML content
     * @return {@link HtmlDocument} instance
     */
    public static HtmlDocument of(long id, URI url, String content){
        return new BasicHtmlDocument(id, url, content);
    }

    /**
     * Creates an {@link HtmlDocument}.
     * @param url URL
     * @param content raw HTML content
     * @return {@link HtmlDocument} instance
     */
    public static HtmlDocument of(URI url, String content){
        return new BasicHtmlDocument(url, content);
    }
}
