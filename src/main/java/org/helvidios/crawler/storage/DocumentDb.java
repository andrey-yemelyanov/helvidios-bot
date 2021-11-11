package org.helvidios.crawler.storage;

import java.net.URI;
import java.util.Objects;
import org.helvidios.crawler.model.HtmlDocument;

/**
 * Persistent storage for downloaded HTML documents. 
 * Supports sequential iteration over all documents, without loading them all at once into memory.
 * The iterator can be used during the indexing stage, where documents are processed one by one.
 */
public interface DocumentDb extends Iterable<HtmlDocument> {

    /**
     * Returns document count in the storage.
     * @throws DocumentDbException if communication with storage fails
     * @return document count
     */
    long size() throws DocumentDbException;

    /**
     * Deletes all documents from the storage.
     * @throws DocumentDbException if communication with storage fails
     */
    void clear() throws DocumentDbException;

    /**
     * Retrieves HTML document with a given ID from storage.
     * @param docId document ID
     * @throws DocumentNotFoundException if document does not exist in storage
     * @throws DocumentDbException if communication with storage fails
     * @return {@code HtmlDocument}
     */
    HtmlDocument get(long docId) throws DocumentNotFoundException, DocumentDbException;

    /**
     * Returns true if a document with a given URL exists in storage.
     * @param url document url
     * @throws DocumentDbException if communication with storage fails
     * @return true if document exists
     */
    boolean contains(URI url) throws DocumentDbException;

    /**
     * Writes an HTML document to the storage.
     * @throws DocumentWriteException if document failed to be written to storage
     * @param doc HTML document
     */
    void write(HtmlDocument doc) throws DocumentWriteException;

    /**
     * Creates a {@link DocumentDb} instance for a specific connection string.
     * <p>Connection string must be in standard URI format:</p>
     * <p>[scheme://][user[:[password]]@]host[:port][/schema][?attribute1=value1&attribute2=value2...</p>
     * <p>Examples:</p>
     * <p>MongoDb: mongodb://localhost:27017/document-db</p>
     * <p><b>NOTE: Currently only MongoDb is supported.</b> Clients are free to use their own implementation of {@link DocumentDb}.</p>
     * @param connectionString connection string in URI format
     * @throws IllegalArgumentException if connection string is invalid or no {@link DocumentDb} provider exists for the supplied connection string
     * @return {@link DocumentDb} instance
     */
    static DocumentDb createFor(URI connectionString) {
        Objects.requireNonNull(connectionString, "connectionString must not be null");
        var scheme = connectionString.getScheme();
        if(scheme == null) throw new IllegalArgumentException(
            String.format("Invalid connection string [%s]. Scheme not found.", connectionString)
        );
        switch(scheme){
            case "mongodb":
                return new MongoDocumentDb(connectionString);
            default:
                throw new IllegalArgumentException(
                    String.format("No provider exists for connection string [%s]", connectionString)
                );
        }
    }
}
