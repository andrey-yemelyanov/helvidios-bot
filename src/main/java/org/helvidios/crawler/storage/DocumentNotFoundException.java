package org.helvidios.crawler.storage;

/**
 * Indicates that a document with a given ID does not exist in storage.
 */
public class DocumentNotFoundException extends DocumentDbException {
    private final long docId;

    DocumentNotFoundException(long docId) {
        super(String.format("Document with ID=%d not found in storage", docId), null);
        this.docId = docId;
    }

    /**
     * Returns ID of the non-existing document.
     * @return document ID
     */
    public long docId(){
        return docId;
    }
    
}
