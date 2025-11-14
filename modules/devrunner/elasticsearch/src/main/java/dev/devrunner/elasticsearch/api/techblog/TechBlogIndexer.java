package dev.devrunner.elasticsearch.api.techblog;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import dev.devrunner.elasticsearch.document.DocBase;
import dev.devrunner.elasticsearch.internal.indexer.AbstractDocIndexer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TechBlogIndexer extends AbstractDocIndexer {

    private final String indexName;

    public TechBlogIndexer(
            ElasticsearchClient esClient,
            @Value("${elasticsearch.index.techblog}") String indexName
    ) {
        super(esClient);
        this.indexName = indexName;
    }

    @Override
    protected String getIndex() {
        return indexName;
    }

    @Override
    protected String getDocId(DocBase doc) {
        return doc.getDocId();
    }

    @Override
    protected boolean validateDoc(DocBase doc) {
        return doc.getDocId() != null;
    }
}
