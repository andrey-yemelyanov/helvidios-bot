package org.helvidios.crawler.http;

import java.net.HttpRetryException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Objects;
import org.helvidios.crawler.model.HtmlDocument;

/**
 * Implements page downloading using {@link HttpClient} from standard library.
 * No rate limiting or retries are supported in this implementation.
 */
class BasicHttpClient implements org.helvidios.crawler.http.HttpClient {
    private final HttpClient httpClient;
    private final Duration requestTimeout;

    BasicHttpClient(Duration requestTimeout){
        this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout must not be null");
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
                .timeout(requestTimeout)
                .build();

            var response = httpClient.send(request, BodyHandlers.ofString());

            // handle 429 Too Many Requests
            if(response.statusCode() == 429){
                throw new TooManyRequestsException(url, response.headers().map());
            }

            if(response.statusCode() != 200) {
                throw new HttpRetryException(
                    String.format("Expecting status code 200 but server returned status code %d", response.statusCode()), 
                    response.statusCode());
            }

            System.out.println("Downloaded " + url);

            return HtmlDocument.of(url, response.body());
        }
        catch(Exception ex){
            throw new FetchException(url, ex);
        }
    }
}
