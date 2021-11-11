package org.helvidios.crawler.storage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Base64;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.helvidios.crawler.model.HtmlDocument;

class CompressedDocumentDb implements DocumentDb {
    private final DocumentDb docDb;

    CompressedDocumentDb(DocumentDb docDb){
        this.docDb = Objects.requireNonNull(docDb, "docDb must not be null");
    }

    private static HtmlDocument compress(HtmlDocument doc) throws IOException {
        if(doc == null) return null;
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

    private static HtmlDocument decompress(HtmlDocument doc) throws IOException {
        if(doc == null) return null;
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

    @Override
    public Iterator<HtmlDocument> iterator() {
        return new Iterator<HtmlDocument>() {
            private Iterator<HtmlDocument> it = docDb.iterator();

            public boolean hasNext() {
                return it.hasNext();
            }

            public HtmlDocument next() {
                try{
                    return decompress(it.next());
                }catch(IOException ex){
                    throw new RuntimeException("Failed to decompress document from iterator", ex);
                }
            }
        };
    }

    @Override
    public long size() throws DocumentDbException {
        return docDb.size();
    }

    @Override
    public void clear() throws DocumentDbException {
        docDb.clear();
    }

    @Override
    public HtmlDocument get(long docId) throws DocumentNotFoundException, DocumentDbException {
        try {
            return decompress(docDb.get(docId));
        } catch (IOException e) {
            throw new DocumentDbException(e);
        }
    }

    @Override
    public boolean contains(URI url) throws DocumentDbException {
        return docDb.contains(url);
    }

    @Override
    public void write(HtmlDocument doc) throws DocumentWriteException {
        try{
            docDb.write(compress(doc));
        }catch(IOException ex){
            throw new DocumentWriteException(doc, ex);
        }
    }
    
}
