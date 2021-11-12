package org.helvidios.crawler.storage;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
     * Returns a builder object for {@link DocumentDb}.
     * @return {@link Builder} object
     */
    public static Builder Builder() {
        return new Builder();
    }

    static class Builder {
        private boolean useCompression;
        private boolean useCaching;
        private DocumentDb storageProvider = new InMemoryDocumentDb();
        private UrlCache urlCache = new GuavaCache();
        private HtmlDocumentCompression compressionProvider = HtmlDocumentCompression.gzip();

        private final Map<String, Function<URI, DocumentDb>> providers = Map.ofEntries(
            Map.entry("mongodb", MongoDocumentDb::new)
        );

        private Builder(){}

        /**
         * Use default GZIP compression of HTML documents stored in the db.
         * @return {@link Builder}
         */
        public Builder withCompression() {
            this.useCompression = true;
            return this;
        }

        /**
         * Use custom compression provider for HTML document compression.
         * @param compressionProvider {@link HtmlDocumentCompression} compression provider
         * @return {@link Builder}
         */
        public Builder withCompression(HtmlDocumentCompression compressionProvider) {
            this.useCompression = true;
            this.compressionProvider = Objects.requireNonNull(compressionProvider, "compressionProvider must not be null");
            return this;
        }

        /**
         * Enable default in-memory caching of URLs based on Guava's cache implementation.
         * @return {@link Builder}
         */
        public Builder withUrlCaching() {
            this.useCaching = true;
            return this;
        }

        /**
         * Enable URL caching using a custom provider.
         * @param urlCache URL caching provider
         * @return {@link Builder}
         */
        public Builder withUrlCaching(UrlCache urlCache) {
            this.useCaching = true;
            this.urlCache = Objects.requireNonNull(urlCache, "urlCache must not be null");
            return this;
        }

        /**
         * Sets storage provider given a specific connection string. This method will attempt to resolve the correct provider.
         * If the provider indicated by the scheme element in the connection string is not supported, an {@link IllegalArgumentException} will be thrown.
         * <p>Connection string must be in standard URI format:</p>
         * <p>[scheme://][user[:[password]]@]host[:port][/schema][?attribute1=value1&attribute2=value2...</p>
         * <p>Examples:</p>
         * <p>MongoDb: mongodb://localhost:27017/document-db</p>
         * <p>Clients are free to use their own implementation of {@link DocumentDb} if their preferred storage engine is not supported.</p>
         * @param connectionString connection string in URI format
         * @throws IllegalArgumentException if connection string is invalid or no provider exists for the supplied connection string
         * @return {@link Builder}
         */
        public Builder withStorageProvider(URI connectionString) {
            Objects.requireNonNull(connectionString, "connectionString must not be null");
            var scheme = connectionString.getScheme();
            if(scheme == null) {
                throw new IllegalArgumentException(
                    String.format("Invalid connection string [%s]. Scheme not found.", connectionString)
                );
            }

            if(!providers.containsKey(scheme)){
                throw new IllegalArgumentException(String.format("No provider exists for connection string [%s]", connectionString));
            }

            this.storageProvider = providers.get(scheme).apply(connectionString);

            return this;
        }


        public DocumentDb build() {
            var docDb = storageProvider;

            if(useCompression) {
                docDb = new DocumentDbWithCompression(compressionProvider, docDb);
            }

            if(useCaching) {
                docDb = new DocumentDbWithUrlCache(urlCache, docDb);
            }

            return docDb;
        }
    }
}
