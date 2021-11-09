package org.helvidios.crawler;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

import com.google.common.base.Throwables;

import org.helvidios.crawler.http.FetchException;
import org.helvidios.crawler.http.HttpClient;
import org.helvidios.crawler.http.HttpClientWithRetry;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     * @throws FetchException
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        var httpClientWithRetry = new HttpClientWithRetry(5, new HttpClient() {

            @Override
            public HtmlDocument fetch(URI url) throws FetchException {
                System.out.printf("%s: Attempting fetch('%s')\n", Instant.now().toString(), url.toString());
                throw new FetchException(url, new IOException("google.com could not be resolved"));
            }
            
        });

        try{
            httpClientWithRetry.fetch(URI.create("http://www.google.com"));
        }
        catch(FetchException ex){
            System.out.println(ex);
            System.out.println(Throwables.getStackTraceAsString(ex));
        }
        
    }
}
