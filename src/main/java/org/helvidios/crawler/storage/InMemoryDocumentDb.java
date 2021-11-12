package org.helvidios.crawler.storage;

import java.net.URI;
import java.util.Iterator;
import org.helvidios.crawler.model.HtmlDocument;

class InMemoryDocumentDb implements DocumentDb {

    @Override
    public Iterator<HtmlDocument> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long size() throws DocumentDbException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void clear() throws DocumentDbException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public HtmlDocument get(long docId) throws DocumentNotFoundException, DocumentDbException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean contains(URI url) throws DocumentDbException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void write(HtmlDocument doc) throws DocumentWriteException {
        // TODO Auto-generated method stub
        
    }
    
}
