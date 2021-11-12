package org.helvidios.crawler.storage;

import java.net.URI;

/**
 * A URL cache that is used for fast lookups of whether a specific URL has already been processed.
 * If the cache contains a URL, then it has already been downloaded and stored in a persistent storage.
 * This should improve performance as a cache hit means no query to a slower persistent store is necessary.
 */
public interface UrlCache {

    /**
     * Adds a document URL to the cache.
     * @param url document URL
     */
    void add(URI url);
    
    /**
     * Checks if the cache contains a given URL.
     * @param url document URL
     * @return true if the given URL exists
     */
    boolean contains(URI url);
}
