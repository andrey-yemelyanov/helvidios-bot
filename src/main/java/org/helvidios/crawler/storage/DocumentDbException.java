package org.helvidios.crawler.storage;

/**
 * Indicates a failure that occured during a document storage operation.
 */
public class DocumentDbException extends Exception {
    
    DocumentDbException(String message, Throwable causedBy) {
        super(message, causedBy);
    }

    DocumentDbException(Throwable causedBy) {
        super("Document storage communication failure occured", causedBy);
    }

}
