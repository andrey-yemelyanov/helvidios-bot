package org.helvidios.crawler.http;

import static org.junit.Assert.assertTrue;
import org.helvidios.crawler.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Category(UnitTest.class)
public class BuilderTests {
    
    @Test
    public void ShouldBuildBasicHttpClient() {
        var httpClient = HttpClient.Builder().build();
        assertTrue("httpClient must be instanceof BasicHttpClient", httpClient instanceof BasicHttpClient);
    }

    @Test
    public void ShouldBuildHttpClientWithRetry() {
        var httpClient = HttpClient.Builder().withRetries(3).build();
        assertTrue("httpClient must be instanceof HttpClientWithRetry", httpClient instanceof HttpClientWithRetry);
    }

    @Test
    public void ShouldBuildHttpClientWithRateLimiter() {
        var httpClient = HttpClient.Builder().withRateLimiter(1).build();
        assertTrue("httpClient must be instanceof HttpClientWithRateLimit", httpClient instanceof HttpClientWithRateLimit);
    }

    @Test
    public void ShouldBuildHttpClientWithRateLimiterAndRetry() {
        var httpClient = HttpClient.Builder().withRateLimiter(1).withRetries(3).build();
        assertTrue("httpClient must be instanceof HttpClientWithRateLimit", httpClient instanceof HttpClientWithRateLimit);
    }
}
