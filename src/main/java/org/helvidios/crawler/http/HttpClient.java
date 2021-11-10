package org.helvidios.crawler.http;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
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
     * Returns builder that can be used for constructing {@link HttpClient}
     * @return {@link Builder} with default parameters
     */
    public static Builder Builder(){
        return new Builder();
    }

    static class Builder {
        private Duration requestTimeout = Duration.ofMinutes(1);
        private Integer retries;
        private RateLimiter rateLimiter;

        private Builder(){}

        /**
         * Set a custom timeout duration for all HTTP requests.
         * @param requestTimeout timeout duration
         * @return {@link Builder}
         */
        public Builder withRequestTimeout(Duration requestTimeout){
            this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout must not be null");
            return this;
        }

        /**
         * Set a custom value for max number of attempts to perform an HTTP request before giving up and throwing an exception.
         * @param retries max number of attempts to perform an HTTP request
         * @throws IllegalArgumentException if retries < 1
         * @return {@link Builder}
         */
        public Builder withRetries(int retries){
            if(retries == 0) throw new IllegalArgumentException("retries must be greater than zero");
            this.retries = retries;
            return this;
        }

        /**
         * Set rate limiter used for limiting max number of HTTP requests per second.
         * @param rateLimiter rate limiter
         * @return {@link Builder}
         */
        public Builder withRateLimiter(RateLimiter rateLimiter){
            this.rateLimiter = Objects.requireNonNull(rateLimiter, "rateLimiter must not be null");;
            return this;
        }

        /**
         * Builds a fully initialized instance of {@code HttpClient}.
         * @return {@code HttpClient}
         */
        public HttpClient build(){
            HttpClient httpClient = new BasicHttpClient(requestTimeout);
            
            if(retries != null) {
                httpClient = new HttpClientWithRetry(retries, httpClient);
            }
            
            if(rateLimiter != null){
                httpClient = new HttpClientWithRateLimit(rateLimiter, httpClient);
            }

            return httpClient;
        }
    }
}
