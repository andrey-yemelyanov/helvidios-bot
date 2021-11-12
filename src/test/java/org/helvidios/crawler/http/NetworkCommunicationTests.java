package org.helvidios.crawler.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.helvidios.crawler.SlowTest;
import org.helvidios.crawler.model.HtmlDocument;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Category(SlowTest.class)
public class NetworkCommunicationTests {

    @Test
    public void ConcurrentRequestsOnHttpClient() throws FetchException, InterruptedException {
        var urls = List.of(
            "https://en.wikipedia.org/wiki/Cimoliopterus",
            "https://en.wikipedia.org/wiki/United_States",
            "https://en.wikipedia.org/wiki/Russia",
            "https://en.wikipedia.org/wiki/Moscow",
            "https://en.wikipedia.org/wiki/Istanbul",
            "https://en.wikipedia.org/wiki/Ancient_Rome",
            "https://en.wikipedia.org/wiki/Ancient_history",
            "https://en.wikipedia.org/wiki/Chinese_characters",
            "https://en.wikipedia.org/wiki/Unicode",
            "https://en.wikipedia.org/wiki/Computer"
        );

        var httpClient = HttpClient.Builder()
                                   .withRequestTimeout(Duration.ofSeconds(10))
                                   .withRetries(3)
                                   .withRateLimiter(20)
                                   .build();
        
        var start = Instant.now();
        for(var url : urls) httpClient.fetch(URI.create(url));
        var end = Instant.now();
        System.out.printf("Single-threaded: Downloaded %d pages in %s\n", urls.size(), Duration.between(start, end));

        var callables = urls.stream().map(url -> new Callable<HtmlDocument>() {
            @Override
            public HtmlDocument call() throws Exception {
                return httpClient.fetch(URI.create(url));
            }
        }).toList();
        var nThreads = Runtime.getRuntime().availableProcessors() * 5;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        start = Instant.now();
        executorService.invokeAll(callables);
        end = Instant.now();
        System.out.printf("Multi-threaded (%d threads): Downloaded %d pages in %s\n", nThreads, urls.size(), Duration.between(start, end));
    }

    @Test
    public void ShouldDownloadWebPageFromInternetUsingRateLimitingAndRetry() throws FetchException {
        final String url = "https://en.wikipedia.org/wiki/Web_crawler";
        var httpClient = HttpClient.Builder()
                                   .withRequestTimeout(Duration.ofSeconds(10))
                                   .withRetries(3)
                                   .withRateLimiter(5)
                                   .build();
        var doc = httpClient.fetch(URI.create(url));
        assertNotNull(doc);
        assertNotNull(doc.content());
        assertEquals(url, doc.url().toString());
    }
    
    @Test
    public void ShouldDownloadWebPageFromInternet() throws FetchException {
        final String url = "https://en.wikipedia.org/wiki/2021_New_York_City_Marathon";
        var httpClient = HttpClient.Builder().build();
        assertTrue("httpClient must be instanceof BasicHttpClient", httpClient instanceof BasicHttpClient);
        var doc = httpClient.fetch(URI.create(url));
        assertNotNull(doc);
        assertNotNull(doc.content());
        assertEquals(url, doc.url().toString());
    }

    @Test(expected = FetchException.class)
    public void ShouldThrowFetchExceptionIfInvalidUrl() throws FetchException {
        final String url = "https://localhost/index.html";
        var httpClient = HttpClient.Builder().withRequestTimeout(Duration.ofSeconds(1)).build();
        httpClient.fetch(URI.create(url));
    }

    @Test
    public void ShouldThrowFetchExceptionAfterTimeout() {
        final String url = "https://httpstat.us/200?sleep=5000";
        var httpClient = HttpClient.Builder().withRequestTimeout(Duration.ofSeconds(1)).build();
        
        try{
            httpClient.fetch(URI.create(url));
            assertTrue("HttpClient must fail due to timeout, but it did not", false);
        }
        catch(FetchException ex){
            assertTrue("Cause exception must be HttpTimeoutException", 
                ex.getCause() instanceof HttpTimeoutException);
        }
    }

    @Test
    public void ShouldNotTimeoutEvenIfServerIsSlow() throws FetchException {
        final String url = "https://httpstat.us/200?sleep=5000";
        var httpClient = HttpClient.Builder().withRequestTimeout(Duration.ofSeconds(10)).build();
        var doc = httpClient.fetch(URI.create(url));
        assertNotNull(doc);
        assertNotNull(doc.content());
        assertEquals(url, doc.url().toString());
    }

}
