package org.helvidios.crawler.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.net.URI;
import org.helvidios.crawler.UnitTest;
import org.helvidios.crawler.model.HtmlDocument;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Category(UnitTest.class)
public class CompressedDocumentDbTests {
    
    private HtmlDocument compressedDocument;

    @Test
    public void ShouldCorrectlyCompressAndDecompressHtmlDocuments() throws DocumentNotFoundException, DocumentDbException {
        var docDbMock = mock(DocumentDb.class);
        final HtmlDocument document = HtmlDocument.of(URI.create("https://www.w3schools.com/html/html_basic.asp"), 
            """
                <!DOCTYPE html>
                <html>
                <body>
                
                <h1>My First Heading</h1>
                <p>My first paragraph.</p>
                
                </body>
                </html>""");
        
        doAnswer(invocation -> {
            compressedDocument = (HtmlDocument) invocation.getArgument(0);
            return null;
        }).when(docDbMock).write(any());

        DocumentDb docDb = new CompressedDocumentDb(docDbMock);
        docDb.write(document);

        when(docDbMock.get(document.docId())).thenReturn(compressedDocument);

        var decompressedDocument = docDb.get(document.docId());

        verify(docDbMock).write(compressedDocument);
        assertNotNull(decompressedDocument);
        assertEquals(document.url(), decompressedDocument.url());
        assertEquals(document.content(), decompressedDocument.content());
        assertEquals(document.docId(), decompressedDocument.docId());
    }
}
