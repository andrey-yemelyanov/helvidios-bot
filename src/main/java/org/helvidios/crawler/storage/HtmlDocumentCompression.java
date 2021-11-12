package org.helvidios.crawler.storage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.helvidios.crawler.model.HtmlDocument;

/**
 * Supports document compression/decompression to save space in storage.
 */
public interface HtmlDocumentCompression {
    
    /**
     * Compresses this document (typically only HTML content) using some algorithm.
     * @param doc HTML document
     * @return compressed {@link HtmlDocument}
     * @throws IOException if compression fails
     */
    HtmlDocument compress(HtmlDocument doc) throws IOException;

    /**
     * Decompresses this document (typically only HTML content) using some algorithm.
     * @param doc HTML document
     * @return decompressed {@link HtmlDocument}
     * @throws IOException if decompression fails
     */
    HtmlDocument decompress(HtmlDocument doc) throws IOException;

    /**
     * Returns a GZIP-based implementation of {@link HtmlDocumentCompression}.
     * @return GZIP implementation
     */
    public static HtmlDocumentCompression gzip() {
        return new HtmlDocumentCompression(){

            @Override
            public HtmlDocument compress(HtmlDocument doc) throws IOException {
                Objects.requireNonNull(doc, "doc must not be null");
                try(var bos = new ByteArrayOutputStream()){
                    try(var gzip = new GZIPOutputStream(bos)){
                        gzip.write(doc.content().getBytes("UTF-8"));
                        gzip.finish();
                        return HtmlDocument.of(
                            doc.docId(), 
                            doc.url(), 
                            Base64.getEncoder().encodeToString(bos.toByteArray()));
                    }
                }
            }
    
            @Override
            public HtmlDocument decompress(HtmlDocument doc) throws IOException {
                Objects.requireNonNull(doc, "doc must not be null");
                byte[] contentBytes = Base64.getDecoder().decode(doc.content());
                try(var bis = new ByteArrayInputStream(contentBytes)){
                    try(var gis = new GZIPInputStream(bis)){
                        try(var br = new BufferedReader(new InputStreamReader(gis, "UTF-8"))){
                            return HtmlDocument.of(
                                doc.docId(), 
                                doc.url(), 
                                br.lines().collect(Collectors.joining("\n")));
                        }
                    }
                }
            }
        };
    }
}
