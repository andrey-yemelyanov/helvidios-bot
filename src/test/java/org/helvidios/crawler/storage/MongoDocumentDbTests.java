package org.helvidios.crawler.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.helvidios.crawler.SlowTest;
import org.helvidios.crawler.model.HtmlDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Category(SlowTest.class)
public class MongoDocumentDbTests {

    private final DocumentDb docDb = 
        DocumentDb.Builder()
                  .withStorageProvider(URI.create("mongodb://localhost:27017/document-db-test"))
                  .build();

    @Before
    public void init() throws DocumentDbException {
        docDb.clear();
    }

    @Test
    public void ShouldWriteDocumentToMongoDb() throws DocumentDbException {
        assertTrue("docDb must be instanceof MongoDocumentDb", docDb instanceof MongoDocumentDb);

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

    @Test
    public void ShouldContainExistingDocument() throws DocumentDbException {
        var doc = HtmlDocument.of(
            URI.create("https://mongodb.github.io/mongo-java-driver/4.3/driver/tutorials/databases-collections/"), 
            "<html></html>"
        );
        docDb.write(doc);
        assertTrue("Document must exist in storage", docDb.contains(doc.url()));
        assertFalse("Document must not exist in storage", docDb.contains(URI.create("http://localhost/doc")));
        assertTrue("Only 1 document must be present in storage", docDb.size() == 1);
    }

    @Test
    public void ShouldIterateOverDocuments() throws DocumentDbException {
        var docs = List.of(
            HtmlDocument.of(URI.create("http://location/1"), "content 1"),
            HtmlDocument.of(URI.create("http://location/2"), "content 2"),
            HtmlDocument.of(URI.create("http://location/3"), "content 3"),
            HtmlDocument.of(URI.create("http://location/4"), "content 4"),
            HtmlDocument.of(URI.create("http://location/5"), "content 5")
        );

        for(var doc : docs) docDb.write(doc);

        assertTrue("Only 5 documents must be present in storage", docDb.size() == 5);

        var storedDocs = new ArrayList<HtmlDocument>();
        docDb.iterator().forEachRemaining(storedDocs::add);

        assertEquals(5, storedDocs.size());
        for(var i = 0; i < storedDocs.size(); i++){
            assertEquals(URI.create("http://location/" + (i + 1)), storedDocs.get(i).url());
            assertEquals("content " + (i + 1), storedDocs.get(i).content());
        }
    }

    @Test
    public void ShouldUtilizeReadWriteLock() {

    }

    @Test
    public void ShouldReadWriteHtmlDocumentFromMongoUsingCompression() throws DocumentNotFoundException, DocumentDbException {

        var docDb = DocumentDb.Builder()
                              .withStorageProvider(URI.create("mongodb://localhost:27017/document-db-test"))
                              .withCompression()
                              .build();

        assertTrue("docDb must be instanceof DocumentDbWithCompression", docDb instanceof DocumentDbWithCompression);

        final HtmlDocument document = HtmlDocument.of(URI.create("https://www.w3schools.com/html/html_basic.asp"), 
            """
                <!DOCTYPE html>
                <html>
                <body>
                
                <h1>My First Heading</h1>
                <p>My first paragraph.</p>
                
                </body>
                </html>""");

        docDb.write(document);
        var decompressedDocument = docDb.get(document.docId());
        assertNotNull(decompressedDocument);
        assertEquals(document.url(), decompressedDocument.url());
        assertEquals(document.content(), decompressedDocument.content());
        assertEquals(document.docId(), decompressedDocument.docId());
    }

    @Test(expected = DocumentNotFoundException.class)
    public void ShouldThrowExceptionIfDocumentNotFound() throws DocumentNotFoundException, DocumentDbException {
        docDb.get(123);
    }
}
