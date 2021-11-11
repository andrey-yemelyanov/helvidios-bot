package org.helvidios.crawler.storage;

import java.net.URI;
import java.util.Iterator;
import java.util.Objects;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.helvidios.crawler.model.HtmlDocument;

class MongoDocumentDb implements DocumentDb {
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    MongoDocumentDb(URI connectionString) {
        Objects.requireNonNull(connectionString, "connectionString must not be null");
        var databaseName = connectionString.getPath();
        if(databaseName == null) throw new IllegalArgumentException(
            String.format("Invalid connection string [%s]. Database name not found.", connectionString)
        );
        var mongoClient = MongoClients.create(connectionString.toString());
        this.database = mongoClient.getDatabase(databaseName.replace("/", ""));
        this.collection = this.database.getCollection("documents");
        this.collection.createIndex(Indexes.ascending("url"));
    }

    @Override
    public long size() throws DocumentDbException {
        return collection.countDocuments();
    }

    @Override
    public void clear() throws DocumentDbException {
        collection.deleteMany(Filters.empty());
    }

    @Override
    public HtmlDocument get(long docId) throws DocumentNotFoundException, DocumentDbException {
        try{
            var doc = collection.find(Filters.eq("_id", docId)).first();
            if(doc == null) throw new DocumentNotFoundException(docId);
            return toHtmlDocument(doc);
        }
        catch(DocumentNotFoundException ex) {
            throw ex;
        }
        catch(Exception ex){
            throw new DocumentDbException(ex);
        }
    }

    @Override
    public boolean contains(URI url) throws DocumentDbException {
        try{
            return collection.find(Filters.eq("url", url.toString())).first() != null;
        }
        catch(Exception ex){
            throw new DocumentDbException(ex);
        }
    }

    @Override
    public void write(HtmlDocument doc) throws DocumentWriteException {
        Objects.requireNonNull(doc, "doc must not be null");
        try{
            var bsonDocument = new Document("_id", doc.docId())
                .append("url", doc.url().toString())
                .append("content", doc.content());
            collection.insertOne(bsonDocument);
        }
        catch(Exception ex){
            throw new DocumentWriteException(doc, ex);
        }
    }

    @Override
    public Iterator<HtmlDocument> iterator() {
        return new Iterator<HtmlDocument>() {
            private Iterator<Document> it = collection.find().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public HtmlDocument next() {
                return toHtmlDocument(it.next());
            }
        };
    }
    
    private static HtmlDocument toHtmlDocument(Document bsonDocument){
        return HtmlDocument.of(
            Long.parseLong(bsonDocument.get("_id").toString()), 
            URI.create(bsonDocument.get("url").toString()),
            bsonDocument.get("content").toString()
        );
    }
}
