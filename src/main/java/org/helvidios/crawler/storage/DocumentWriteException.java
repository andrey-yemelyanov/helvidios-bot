package org.helvidios.crawler.storage;

import java.util.Objects;
import org.helvidios.crawler.model.HtmlDocument;

/**
 * Indicates a failure to write an HTML document to storage.
 */
public class DocumentWriteException extends DocumentDbException {
    private final HtmlDocument document;

    DocumentWriteException(HtmlDocument document, Throwable causedBy) {
        super(String.format("Write to storage for document [%s] failed", 
            Objects.requireNonNull(document, "document must not be null").url()), 
            causedBy);
        this.document = document;
    }
    
    /**
     * Returns HTML document which failed to be written to storage.
     * @return HTML document
     */
    public HtmlDocument document(){
        return document;
    }
}
