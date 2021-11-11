package org.helvidios.crawler.storage;

import static org.junit.Assert.assertTrue;
import java.net.URI;
import org.helvidios.crawler.UnitTest;
import org.helvidios.crawler.storage.DocumentDb.CompressionOptions;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Category(UnitTest.class)
public class DocumentDbTests {
    
    @Test
    public void ShouldThrowExceptionIfNoSchemeFound() {
        var connectionString = URI.create("localhost/document-db-test");
        try{
            DocumentDb.createFor(connectionString, CompressionOptions.DoNotCompressDocumentContent);
            assertTrue("Must throw exception on connection string without scheme" + connectionString, false);
        }catch(IllegalArgumentException ex){
            assertTrue(ex.getMessage().contains("Scheme not found"));
        }
    }

    @Test
    public void ShouldThrowExceptionIfProviderNotSupported() {
        var connectionString = URI.create("mssql://localhost:123/MyDbInstance/MyDbName?FailoverPartner=None&InboundId=123");
        try{
            DocumentDb.createFor(connectionString, CompressionOptions.DoNotCompressDocumentContent);
            assertTrue("Must throw exception on unsupported connection string " + connectionString, false);
        }catch(IllegalArgumentException ex){
            assertTrue(ex.getMessage().contains("No provider exists for connection string"));
        }
    }

    @Test(expected = NullPointerException.class)
    public void ShouldThrowExceptionIfConnectionStringIsNull() {
        DocumentDb.createFor(null, CompressionOptions.DoNotCompressDocumentContent);
    }
}
