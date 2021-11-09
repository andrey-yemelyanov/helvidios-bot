package org.helvidios.crawler.http;

import java.net.URI;
import com.google.common.util.concurrent.RateLimiter;
import org.helvidios.crawler.HtmlDocument;

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
     * Returns a default instance of {@link HttpClient}.
     * @return default {@link HttpClient} instance
     */
    public static HttpClient basic(){
        return new BasicHttpClient();
    }

    /**
     * Returns an instance of {@link HttpClient} that supports configurable retries with exponential backoff and jitter.
     * @param retries number of retries when downloading a web page
     * @return retriable {@link HttpClient} instance
     */
    public static HttpClient withRetry(int retries){
        return new HttpClientWithRetry(retries, basic());
    }

    /**
     * Returns an instance of {@link HttpClient} that supports rate limiting i.e max number of HTTP requests per second.
     * @param rateLimiter shared rate limiter that will be used on this client
     * @return {@link HttpClient} instance with rate limiting
     */
    public static HttpClient withRateLimit(RateLimiter rateLimiter){
        return new HttpClientWithRateLimit(rateLimiter, basic());
    }

    /**
     * Returns an instance of {@link HttpClient} that supports both rate limiting and retries.
     * @param retries number of retries when downloading a web page
     * @param rateLimiter shared rate limiter that will be used on this client
     * @return {@link HttpClient} instance with rate limiting and support for retries
     */
    public static HttpClient withRetryAndRateLimit(int retries, RateLimiter rateLimiter){
        return new HttpClientWithRateLimit(rateLimiter, withRetry(retries));
    }
}
