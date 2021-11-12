package org.helvidios.crawler.storage;

import java.net.URI;
import java.util.Iterator;
import java.util.Objects;
import org.helvidios.crawler.model.HtmlDocument;

class DocumentDbWithUrlCache implements DocumentDb {
    private final UrlCache urlCache;
    private final DocumentDb docDb;

    DocumentDbWithUrlCache(UrlCache urlCache, DocumentDb docDb) {
        this.urlCache = Objects.requireNonNull(urlCache, "urlCache must not be null");
        this.docDb = Objects.requireNonNull(docDb, "docDb must not be null");
    }

    @Override
    public Iterator<HtmlDocument> iterator() {
        return docDb.iterator();
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
        return docDb.get(docId);
    }

    @Override
    public boolean contains(URI url) throws DocumentDbException {
        if(!urlCache.contains(url)) {
            var urlProcessed = docDb.contains(url);
            if (urlProcessed) {
                urlCache.add(url);
            }
        }

        return urlCache.contains(url);
    }

    @Override
    public void write(HtmlDocument doc) throws DocumentWriteException {
        docDb.write(doc);
        urlCache.add(doc.url());
    }
}
