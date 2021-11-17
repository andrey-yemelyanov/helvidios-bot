package org.helvidios.crawler.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.helvidios.crawler.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Category(UnitTest.class)
public class TooManyRequestsExceptionTests {

    @Test
    public void ShouldReturnDefaultWaitIfNotHeaderPresent(){
        var ex = new TooManyRequestsException(
            URI.create("https://www.google.com"), 
            Map.ofEntries());

        assertEquals(TooManyRequestsException.DEFAULT_RETRY, ex.retryAfter());
    }

    @Test
    public void ShouldParseRetryAfterSeconds(){
        var ex = new TooManyRequestsException(
            URI.create("https://www.google.com"), 
            Map.ofEntries(
                Map.entry("Retry-After", List.of("360"))
            ));

        assertEquals(360, ex.retryAfter());
    }

    @Test
    public void ShouldReturnDefaultWaitIfDateNotValid(){
        final String retryAfterDate = "2019-02-28";

        var ex = new TooManyRequestsException(
            URI.create("https://www.google.com"), 
            Map.ofEntries(
                Map.entry("Retry-After", List.of(retryAfterDate))
            ));

        assertEquals(TooManyRequestsException.DEFAULT_RETRY, ex.retryAfter());
    }

    @Test
    public void ShouldReturnDefaultWaitIfInvalidSeconds(){
        var ex = new TooManyRequestsException(
            URI.create("https://www.google.com"), 
            Map.ofEntries(
                Map.entry("Retry-After", List.of("360 seconds"))
            ));

        assertEquals(TooManyRequestsException.DEFAULT_RETRY, ex.retryAfter());
    }
    
    @Test
    public void ShouldParsRetryAfterDate() {

        final String retryAfterDate = "Wed, 21 Oct 2025 07:28:00 GMT";

        var ex = new TooManyRequestsException(
            URI.create("https://www.google.com"), 
            Map.ofEntries(
                Map.entry("Retry-After", List.of(retryAfterDate))
            ));

        assertTrue(ex.retryAfter() > 100_000_000);
    }
}
