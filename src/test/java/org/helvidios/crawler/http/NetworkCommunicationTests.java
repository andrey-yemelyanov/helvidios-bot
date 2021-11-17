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
import java.util.concurrent.ExecutionException;
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
    public void TooManyRequests() throws FetchException {
        var httpClient = HttpClient.Builder()
                                   .withRequestTimeout(Duration.ofSeconds(10))
                                   .withRetries(3)
                                   .withRateLimiter(200)
                                   .build();

        var doc = httpClient.fetch(URI.create("http://httpstat.us/429"));
        doc = httpClient.fetch(URI.create("http://httpstat.us/200"));
    }

    @Test
    public void ConcurrentRequestsOnHttpClient() throws FetchException, InterruptedException, ExecutionException {
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
            "https://en.wikipedia.org/wiki/Computer",

            "https://en.wikipedia.org/wiki/Computer_cluster",
            "https://en.wikipedia.org/wiki/Amdahl%27s_law",
            "https://en.wikipedia.org/wiki/Moore's_law",
            "https://en.wikipedia.org/wiki/Transistor",
            "https://en.wikipedia.org/wiki/Nobel_Prize",
            "https://en.wikipedia.org/wiki/Sweden",
            "https://en.wikipedia.org/wiki/Stockholm",
            "https://en.wikipedia.org/wiki/List_of_countries_by_GDP_(nominal)_per_capita",
            "https://en.wikipedia.org/wiki/United_States_dollar",
            "https://en.wikipedia.org/wiki/Monetary_policy_of_the_United_States",

            "https://en.wikipedia.org/wiki/Uzbekistan",
            "https://en.wikipedia.org/wiki/Kazakhstan",
            "https://en.wikipedia.org/wiki/Landlocked_country",
            "https://en.wikipedia.org/wiki/Human_Development_Index",
            "https://en.wikipedia.org/wiki/United_Kingdom",
            "https://en.wikipedia.org/wiki/List_of_countries_by_GDP_(nominal)",
            "https://en.wikipedia.org/wiki/Economy_of_Spain",
            "https://en.wikipedia.org/wiki/European_Union",
            "https://en.wikipedia.org/wiki/Europe",
            "https://en.wikipedia.org/wiki/Latitude",

            "https://en.wikipedia.org/wiki/Geography",
            "https://en.wikipedia.org/wiki/Ancient_Greek",
            "https://en.wikipedia.org/wiki/Renaissance",
            "https://en.wikipedia.org/wiki/German_language",
            "https://en.wikipedia.org/wiki/Germanic_languages",
            "https://en.wikipedia.org/wiki/Scandinavia",
            "https://en.wikipedia.org/wiki/Greenland",
            "https://en.wikipedia.org/wiki/Christopher_Columbus",
            "https://en.wikipedia.org/wiki/Republic_of_Genoa",
            "https://en.wikipedia.org/wiki/Mediterranean_Sea",

            "https://en.wikipedia.org/wiki/Sea",
            "https://en.wikipedia.org/wiki/Ocean",
            "https://en.wikipedia.org/wiki/Pacific_Ocean",
            "https://en.wikipedia.org/wiki/Western_Hemisphere",
            "https://en.wikipedia.org/wiki/Americas",
            "https://en.wikipedia.org/wiki/Asia",
            "https://en.wikipedia.org/wiki/Classical_antiquity",
            "https://en.wikipedia.org/wiki/History",
            "https://en.wikipedia.org/wiki/King_Arthur",
            "https://en.wikipedia.org/wiki/Lancelot"
        );

        var httpClient = HttpClient.Builder()
                                   .withRequestTimeout(Duration.ofSeconds(10))
                                   .withRetries(3)
                                   .withRateLimiter(200)
                                   .build();
        
        var start = Instant.now();
        for(var url : urls) {
            httpClient.fetch(URI.create(url));
        }
        var end = Instant.now();
        System.out.printf("Single-threaded: Downloaded %d pages in %s\n", urls.size(), Duration.between(start, end));
        System.out.println();

        var callables = urls.stream().map(url -> new Callable<HtmlDocument>() {
            @Override
            public HtmlDocument call() throws Exception {
                return httpClient.fetch(URI.create(url));
            }
        }).toList();
        final int THREADS_PER_CORE = 10;
        var nThreads = Runtime.getRuntime().availableProcessors() * THREADS_PER_CORE;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        start = Instant.now();
        var futures = executorService.invokeAll(callables);
        end = Instant.now();
        System.out.printf("Multi-threaded (%d threads): Downloaded %d pages in %s\n", nThreads, urls.size(), Duration.between(start, end));
        
        // compute total download size
        double totalSizeInBytes = 0;
        for(var future : futures){
            totalSizeInBytes += future.get().content().getBytes().length;
        }
        System.out.printf("Downloaded %.2f MB\n", totalSizeInBytes / (1024 * 1024));
    }

    @Test(expected = FetchException.class)
    public void ShouldThrowExceptionIfWikipediaPageNotFound() throws FetchException {
        final String url = "https://en.wikipedia.org/wiki/non-existing-page";
        var httpClient = HttpClient.Builder()
                                   .withRequestTimeout(Duration.ofSeconds(3))
                                   .withRetries(3)
                                   .withRateLimiter(5)
                                   .build();
        httpClient.fetch(URI.create(url));
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
