package org.helvidios.crawler.storage;

import static org.junit.Assert.assertEquals;
import java.net.URI;
import org.helvidios.crawler.SlowTest;
import org.helvidios.crawler.model.HtmlDocument;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Category(SlowTest.class)
public class MongoDocumentDbTests {

    private final DocumentDb docDb = DocumentDb.createFor(URI.create("mongodb://localhost:27017/document-db-test"));
    
    @Test
    public void ShouldConnectToMongoDbInstance() throws DocumentDbException {
        assertEquals(0, docDb.size());
    }

    @Test
    public void ShouldWriteDocumentToMongoDb() throws DocumentNotFoundException, DocumentDbException {
        var doc = HtmlDocument.of(
            URI.create("https://mongodb.github.io/mongo-java-driver/4.3/driver/tutorials/databases-collections/"), 
            "<html></html>"
        );
        docDb.write(doc);
        var docInDb = docDb.get(doc.docId());
        assertEquals(doc.docId(), docInDb.docId());
        assertEquals(doc.url(), docInDb.url());
        assertEquals(doc.content(), docInDb.content());
    }
}
