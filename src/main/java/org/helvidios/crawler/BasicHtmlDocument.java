package org.helvidios.crawler;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Implements {@link HtmlDocument}.
 */
class BasicHtmlDocument implements HtmlDocument {
    private final String content;
    private final URI url;
    private final long id;

    BasicHtmlDocument(long id, URI url, String content){
        this.id = id;
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
    }

    BasicHtmlDocument(URI url, String content){
        this(0, url, content);
    }

    @Override
    public String content() {
        return content;
    }

    @Override
    public URI url() {
        return url;
    }

    @Override
    public long docId() {
        return id;
    }

    @Override
    public List<URI> urls() {
        return Collections.emptyList();
    }
    
    @Override
    public String toString() {
        final String separator = "\n\n" + String.join("", Collections.nCopies(100, "*")) + "\n\n";
        return String.format(
            "[%s]%s%s%s\n", 
            url(), separator, content(), separator
        );
    }
}
