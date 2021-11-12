package org.helvidios.crawler.storage;

import static org.junit.Assert.assertTrue;
import java.net.URI;
import org.helvidios.crawler.SlowTest;
import org.helvidios.crawler.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Category(UnitTest.class)
public class DocumentDbTests {

    @Test
    public void ShouldBuildDefaultInMemoryProvider() {
        var docDb = DocumentDb.Builder().build();
        assertTrue("docDb must be instanceof InMemoryDocumentDb", 
            docDb instanceof InMemoryDocumentDb);
    }

    @Test
    @Category(SlowTest.class)
    public void ShouldBuildWithMongoProvider() {
        var docDb = DocumentDb.Builder()
            .withStorageProvider(URI.create("mongodb://localhost:27017/document-db-test"))
            .build();
        assertTrue("docDb must be instanceof MongoDocumentDb", 
            docDb instanceof MongoDocumentDb);
    }

    @Test
    public void ShouldBuildWithCompression() {
        var docDb = DocumentDb.Builder()
            .withCompression()
            .build();
        assertTrue("docDb must be instanceof DocumentDbWithCompression", 
            docDb instanceof DocumentDbWithCompression);
    }

    @Test
    public void ShouldBuildWithUrlCaching() {
        var docDb = DocumentDb.Builder()
            .withUrlCaching()
            .build();
        assertTrue("docDb must be instanceof DocumentDbWithUrlCache", 
            docDb instanceof DocumentDbWithUrlCache);
    }
    
    @Test
    public void ShouldThrowExceptionIfNoSchemeFound() {
        var connectionString = URI.create("localhost/document-db-test");
        try{
            DocumentDb.Builder().withStorageProvider(connectionString).build();
            assertTrue("Must throw exception on connection string without scheme" + connectionString, false);
        }catch(IllegalArgumentException ex){
            assertTrue(ex.getMessage().contains("Scheme not found"));
        }
    }

    @Test
    public void ShouldThrowExceptionIfProviderNotSupported() {
        var connectionString = URI.create("mssql://localhost:123/MyDbInstance/MyDbName?FailoverPartner=None&InboundId=123");
        try{
            DocumentDb.Builder().withStorageProvider(connectionString).build();
            assertTrue("Must throw exception on unsupported connection string " + connectionString, false);
        }catch(IllegalArgumentException ex){
            assertTrue(ex.getMessage().contains("No provider exists for connection string"));
        }
    }

    @Test(expected = NullPointerException.class)
    public void ShouldThrowExceptionIfConnectionStringIsNull() {
        DocumentDb.Builder().withStorageProvider(null).build();
    }
}
