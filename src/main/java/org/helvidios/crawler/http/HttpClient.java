package org.helvidios.crawler.http;

import java.net.URI;
import java.time.Duration;
import com.google.common.util.concurrent.RateLimiter;
import org.helvidios.crawler.model.HtmlDocument;

/**
 * Responsible for all HTTP traffic with external resources.
 */
public interface HttpClient {
    
    /**
     * Downloads raw web page content from a supplied URL. 
     * This method is synchronous and will block the calling thread until the network communication is completed.
     * @param url URL from which the page will be fetched
     * @return downloaded HTML document
     * @throws FetchException if network communication error occurs
     */
    HtmlDocument fetch(URI url) throws FetchException;

    /**
     * Returns a default instance of {@link HttpClient}. This is a bare-bones implementation
     * without support for retries or rate limiting.
     * @param requestTimeout time duration to wait before an HTTP request times out
     * @return default {@link HttpClient} instance
     */
    public static HttpClient basic(Duration requestTimeout){
        return new BasicHttpClient(requestTimeout);
    }

    /**
     * Returns an instance of {@link HttpClient} that supports both rate limiting and retries.
     * @param requestTimeout time duration to wait before an HTTP request times out
     * @param retries number of attempts to download a web page in case of communication failure
     * @param rateLimiter shared rate limiter that will limit a max number of requests per second issued by this client
     * @return {@link HttpClient} instance with rate limiting and support for retries
     */
    public static HttpClient withRetryAndRateLimit(Duration requestTimeout, int retries, RateLimiter rateLimiter){
        return new HttpClientWithRateLimit(rateLimiter, 
            new HttpClientWithRetry(retries, basic(requestTimeout)));
    }
}
