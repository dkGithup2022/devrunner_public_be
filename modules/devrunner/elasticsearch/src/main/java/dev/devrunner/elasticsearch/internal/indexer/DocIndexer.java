package dev.devrunner.elasticsearch.internal.indexer;

import dev.devrunner.elasticsearch.document.DocBase;

public interface DocIndexer {
    IndexResponseType indexOne(DocBase doc);


}
