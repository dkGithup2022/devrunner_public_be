package dev.devrunner.elasticsearch.api.job;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import dev.devrunner.elasticsearch.document.DocBase;
import dev.devrunner.elasticsearch.internal.indexer.AbstractDocIndexer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JobIndexer extends AbstractDocIndexer {

    private final String indexName;

    public JobIndexer(
            ElasticsearchClient esClient,
            @Value("${elasticsearch.index.job}") String indexName
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
