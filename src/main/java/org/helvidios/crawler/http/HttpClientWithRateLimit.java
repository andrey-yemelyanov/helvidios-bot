package org.helvidios.crawler.http;

import java.net.URI;
import java.util.Objects;
import com.google.common.util.concurrent.RateLimiter;
import org.helvidios.crawler.model.HtmlDocument;

/**
 * An implementation of {@link HttpClient} with rate limiting based on Guava's {@link RateLimiter}.
 */
class HttpClientWithRateLimit implements HttpClient {
    private final RateLimiter rateLimiter;
    private final HttpClient httpClient;

    /**
     * Creates a new instance of {@link HttpClientWithRateLimit}
     * @param httpClient underlying http client to which actual page downloading will be delegated
     */
    HttpClientWithRateLimit(int requestsPerSecond, HttpClient httpClient){
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.rateLimiter = RateLimiter.create(requestsPerSecond);
    }

    @Override
    public HtmlDocument fetch(URI url) throws FetchException {
        rateLimiter.acquire();
        return httpClient.fetch(url);
    }
}
