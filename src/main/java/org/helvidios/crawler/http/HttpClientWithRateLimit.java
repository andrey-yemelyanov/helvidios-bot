package org.helvidios.crawler.http;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.google.common.util.concurrent.RateLimiter;
import org.helvidios.crawler.model.HtmlDocument;

/**
 * An implementation of {@link HttpClient} with rate limiting based on Guava's {@link RateLimiter}.
 */
class HttpClientWithRateLimit implements HttpClient {
    private final RateLimiter rateLimiter;
    private final HttpClient httpClient;

    private final Lock lock = new ReentrantLock();
    private final Condition signal = lock.newCondition();
    private boolean blocked = false;

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
        try{
            awaitWhileBlocked();
            rateLimiter.acquire();
            return httpClient.fetch(url);
        }
        catch(TooManyRequestsException ex){
            blockAllRequests();
            var jitter = ThreadLocalRandom.current().nextInt(200, 1500);
            await((ex.retryAfter() * 1000) + jitter);
            unblockAllRequests();
            throw ex;
        }
    }

    private void await(long waitTime){
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {}
    }

    private void awaitWhileBlocked(){
        try{
            lock.lock();
            while(blocked){
                try{
                    signal.await();
                }catch(InterruptedException ex){}
            }
        }
        finally{
            lock.unlock();
        }
    }

    private void blockAllRequests(){
        try{
            lock.lock();
            blocked = true;
        }
        finally{
            lock.unlock();
        }
    }

    private void unblockAllRequests(){
        try{
            lock.lock();
            blocked = false;
            signal.signalAll();
        }
        finally{
            lock.unlock();
        }
    }
}
