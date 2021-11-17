package org.helvidios.crawler.http;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.Objects;

/**
 * Indicates that the server responded with status 429 Too Many Requests.
 * The client needs to back off and wait some time before sending new requests.
 */
public class TooManyRequestsException extends FetchException {
    private final Map<String, List<String>> headers;

    /**
     * Default wait duration in seconds before re-attempting an HTTP request.
     */
    public static final int DEFAULT_RETRY = 5;

    TooManyRequestsException(URI url, Map<String, List<String>> headers) {
        super(url, null);
        this.headers = Objects.requireNonNull(headers, "headers must not be null");
    }

    /**
     * Returns amount of time to wait in seconds before re-attempting the request.
     * @return wait in seconds
     */
    public long retryAfter(){
        long retryAfterSeconds = DEFAULT_RETRY;

        final String retryAfterHeader = "retry-after";
        for(var header : headers.keySet()){
            if(header.toLowerCase().trim().equals(retryAfterHeader)){
                /*
                    Retry-After syntax:
                        Retry-After: <http-date> e.g. Retry-After: Wed, 21 Oct 2015 07:28:00 GMT
                        Retry-After: <delay-seconds> e.g. Retry-After: 120
                */

                if(headers.get(header).size() < 1) break;

                final String retryAfterHeaderValue = headers.get(header).get(0);

                try{
                    retryAfterSeconds = Duration.between(
                        Instant.now(), 
                        parseDate(retryAfterHeaderValue)).toSeconds();
                }catch(ParseException ex){
                    System.out.println(ex);
                    try{
                        retryAfterSeconds = Integer.parseInt(retryAfterHeaderValue);
                    }catch(NumberFormatException nfex){
                        System.out.println(nfex);
                    }
                }

                break;
            }
        }

        return retryAfterSeconds;
    }

    private static Instant parseDate(String date) throws ParseException {
        var df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.parse(date).toInstant();
    }
}
