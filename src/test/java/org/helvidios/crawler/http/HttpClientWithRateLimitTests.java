package org.helvidios.crawler.http;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import org.helvidios.crawler.UnitTest;
import org.helvidios.crawler.model.HtmlDocument;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
@Category(UnitTest.class)
public class HttpClientWithRateLimitTests {
    
    private final URI url = URI.create("http://www.google.com");
    private final String content = "<html><body>hello</body></html>";

    @Test
    public void ShouldRateLimitRequests() throws FetchException, InterruptedException {

        final Map<Long, Integer> requests = new HashMap<>();
        final int CONCURRENT_CLIENTS = 50;
        final int QPS = 10;
        final Lock lock = new ReentrantLock();

        var httpClientMock = mock(HttpClient.class);
        when(httpClientMock.fetch(any())).thenAnswer(new Answer<HtmlDocument>() {

            @Override
            public HtmlDocument answer(InvocationOnMock invocation) throws Throwable {
                try{
                    lock.lock();
                    var timestamp = Instant.now().getEpochSecond();
                    requests.put(timestamp, requests.getOrDefault(timestamp, 0) + 1);
                }finally{
                    lock.unlock();
                }

                return HtmlDocument.of(url, content);
            }
            
        });

        var callables = 
            IntStream.range(0, CONCURRENT_CLIENTS)
                     .mapToObj(i -> new HttpClientWithRateLimit(QPS, httpClientMock))
                     .map(this::toCallable)
                     .toList();

        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_CLIENTS);
        var docs = toDocs(executorService.invokeAll(callables));
        
        verify(httpClientMock, times(CONCURRENT_CLIENTS)).fetch(url);
        assertEquals(CONCURRENT_CLIENTS, docs.size());
        assertEquals(CONCURRENT_CLIENTS, requests.values().stream().mapToInt(i -> i).sum());
    }

    private List<HtmlDocument> toDocs(List<Future<HtmlDocument>> futures){
        return futures.stream().map(future -> {
            try{
                return future.get();
            }catch(Exception ex){
                return null;
            }
        }).toList();
    }

    private Callable<HtmlDocument> toCallable(HttpClient httpClient) {
        return new Callable<HtmlDocument>() {
            @Override
            public HtmlDocument call() throws Exception {
                return httpClient.fetch(url);
            }
        };
    }
}
