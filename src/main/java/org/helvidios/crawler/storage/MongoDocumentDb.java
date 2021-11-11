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
        // TODO Auto-generated method stub
        
    }

    @Override
    public HtmlDocument get(long docId) throws DocumentNotFoundException, DocumentDbException {
        try{
            var doc = collection.find(Filters.eq("_id", docId)).first();
            if(doc == null) throw new DocumentNotFoundException(docId);
            return HtmlDocument.of(
                Long.parseLong(doc.get("_id").toString()), 
                URI.create(doc.get("url").toString()),
                doc.get("content").toString()
            );
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
        // TODO Auto-generated method stub
        return false;
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
        // TODO Auto-generated method stub
        return null;
    }
    
}
