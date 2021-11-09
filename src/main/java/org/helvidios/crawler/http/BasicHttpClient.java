package org.helvidios.crawler.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import org.helvidios.crawler.HtmlDocument;

/**
 * Implements page downloading using {@link HttpClient} from standard library.
 */
class BasicHttpClient implements org.helvidios.crawler.http.HttpClient {
    private final HttpClient httpClient;

    BasicHttpClient(){
        this.httpClient = HttpClient.newBuilder()
            .version(Version.HTTP_2)
            .followRedirects(Redirect.NORMAL)
            .build();
    }

    @Override
    public HtmlDocument fetch(URI url) throws FetchException {
        try{
            
            var request = HttpRequest.newBuilder()
                .uri(url)
                .timeout(Duration.ofMinutes(1))
                .build();

            var response = httpClient.send(request, BodyHandlers.ofString());

            return HtmlDocument.of(url, response.body());
        }
        catch(Exception ex){
            throw new FetchException(url, ex);
        }
    }
    
}
