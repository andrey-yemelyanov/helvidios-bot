package org.helvidios.crawler.http;

import org.helvidios.crawler.UnitTest;
import org.helvidios.crawler.model.HtmlDocument;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.net.URI;

@RunWith(MockitoJUnitRunner.class)
@Category(UnitTest.class)
public class HttpClientWithRetryTests {

    private final URI url = URI.create("http://www.google.com");
    private final String content = "<html><body>hello</body></html>";

    @Test
    public void ShouldNotRetryWhenServerResponds() throws FetchException {

        var httpClientMock = mock(HttpClient.class);
        when(httpClientMock.fetch(any())).thenReturn(HtmlDocument.of(url, content));

        var httpClientWithRetry = new HttpClientWithRetry(3, httpClientMock);
        var doc = httpClientWithRetry.fetch(url);

        verify(httpClientMock).fetch(url);
        assertEquals(url, doc.url());
        assertEquals(content, doc.content());
    }

    @Test
    public void ShouldRetryFourTimesWhenFetchExceptionOccurs() throws FetchException {
        
        var httpClientMock = mock(HttpClient.class);
        when(httpClientMock.fetch(url))
            .thenThrow(new FetchException(url, new IOException()))
            .thenThrow(new FetchException(url, new IOException()))
            .thenThrow(new FetchException(url, new IOException()))
            .thenReturn(HtmlDocument.of(url, content));

        var httpClientWithRetry = new HttpClientWithRetry(5, httpClientMock);
        var doc = httpClientWithRetry.fetch(url);

        verify(httpClientMock, times(4)).fetch(url);
        assertEquals(url, doc.url());
        assertEquals(content, doc.content());
    }

    @Test(expected = FetchException.class)
    public void ShouldThrowFetchExceptionAfterAllRetriesFailed() throws FetchException {

        var httpClientMock = mock(HttpClient.class);
        when(httpClientMock.fetch(url))
            .thenThrow(new FetchException(url, new IOException()))
            .thenThrow(new FetchException(url, new IOException()))
            .thenThrow(new FetchException(url, new IOException()))
            .thenReturn(HtmlDocument.of(url, content));

        var httpClientWithRetry = new HttpClientWithRetry(3, httpClientMock);
        httpClientWithRetry.fetch(url);
    }
}
