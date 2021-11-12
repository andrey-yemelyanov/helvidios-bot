package org.helvidios.crawler.storage;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Objects;
import org.helvidios.crawler.model.HtmlDocument;

class DocumentDbWithCompression implements DocumentDb {
    private final DocumentDb docDb;
    private final HtmlDocumentCompression compressor;

    DocumentDbWithCompression(HtmlDocumentCompression compressor, DocumentDb docDb){
        this.docDb = Objects.requireNonNull(docDb, "docDb must not be null");
        this.compressor = Objects.requireNonNull(compressor, "compressor must not be null");
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
                    return compressor.decompress(it.next());
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
            return compressor.decompress(docDb.get(docId));
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
            docDb.write(compressor.compress(doc));
        }catch(IOException ex){
            throw new DocumentWriteException(doc, ex);
        }
    }
    
}
