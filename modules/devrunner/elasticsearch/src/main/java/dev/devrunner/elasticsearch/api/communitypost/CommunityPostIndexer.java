package dev.devrunner.elasticsearch.api.communitypost;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import dev.devrunner.elasticsearch.document.DocBase;
import dev.devrunner.elasticsearch.internal.indexer.AbstractDocIndexer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommunityPostIndexer extends AbstractDocIndexer {

    private final String indexName;

    public CommunityPostIndexer(
            ElasticsearchClient esClient,
            @Value("${elasticsearch.index.communitypost}") String indexName
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
