package org.helvidios.crawler.http;

import java.net.URI;
import java.util.Objects;
import org.helvidios.crawler.model.HtmlDocument;
import io.github.resilience4j.retry.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.CheckedFunction1;

class HttpClientWithRetry implements HttpClient {
    private final HttpClient httpClient;
    private final Retry fetchRetry;

    private final int INITIAL_INTERVAL = 500;
    private final double MULTIPLIER = 1.75;
    private final double RANDOMIZATION_FACTOR = 0.5;

    HttpClientWithRetry(int retries, HttpClient httpClient){
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");

        var intervalFn = IntervalFunction.ofExponentialRandomBackoff(INITIAL_INTERVAL, MULTIPLIER, RANDOMIZATION_FACTOR);

        var retryConfig = RetryConfig.custom()
            .maxAttempts(retries)
            .intervalFunction(intervalFn)
            .build();
        
        fetchRetry = Retry.of("fetch", retryConfig);
    }

    @Override
    public HtmlDocument fetch(URI url) throws FetchException {
        
        CheckedFunction1<URI, HtmlDocument> fetchFunction = Retry.decorateCheckedFunction(
            fetchRetry, httpClient::fetch);
        
        try{
            return fetchFunction.apply(url);
        }
        catch(Throwable t){
            if(t instanceof FetchException ex){
                throw ex;
            }

            throw new FetchException(url, t);
        }
    }
}
