package org.helvidios.crawler.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import com.google.common.util.concurrent.RateLimiter;
import org.helvidios.crawler.SlowTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Category(SlowTest.class)
public class BasicHttpClientTests {

    @Test
    public void ShouldDownloadWebPageFromInternetUsingRateLimitingAndRetry() throws FetchException {
        final String url = "https://en.wikipedia.org/wiki/Web_crawler";
        var httpClient = HttpClient.withRetryAndRateLimit(Duration.ofSeconds(1), 3, RateLimiter.create(5));
        var doc = httpClient.fetch(URI.create(url));
        assertNotNull(doc);
        assertNotNull(doc.content());
        assertEquals(url, doc.url().toString());
    }
    
    @Test
    public void ShouldDownloadWebPageFromInternet() throws FetchException {
        final String url = "https://en.wikipedia.org/wiki/2021_New_York_City_Marathon";
        var httpClient = HttpClient.basic(Duration.ofMillis(1000));
        var doc = httpClient.fetch(URI.create(url));
        assertNotNull(doc);
        assertNotNull(doc.content());
        assertEquals(url, doc.url().toString());
    }

    @Test(expected = FetchException.class)
    public void ShouldThrowFetchExceptionIfInvalidUrl() throws FetchException {
        final String url = "https://localhost/index.html";
        var httpClient = HttpClient.basic(Duration.ofMillis(1000));
        httpClient.fetch(URI.create(url));
    }

    @Test
    public void ShouldThrowFetchExceptionAfterTimeout() {
        final String url = "https://httpstat.us/200?sleep=5000";
        var httpClient = HttpClient.basic(Duration.ofMillis(1000));
        
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
        var httpClient = HttpClient.basic(Duration.ofMillis(7000));
        var doc = httpClient.fetch(URI.create(url));
        assertNotNull(doc);
        assertNotNull(doc.content());
        assertEquals(url, doc.url().toString());
    }

}
