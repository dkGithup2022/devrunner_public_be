package dev.devrunner.elasticsearch.api.communitypost;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.elasticsearch.api.AbstractDocPopularityManager;
import dev.devrunner.elasticsearch.document.CommunityPostDoc;
import dev.devrunner.elasticsearch.internal.indexer.DocIndexer;
import dev.devrunner.model.common.Popularity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommunityPostPopularityManager extends AbstractDocPopularityManager<CommunityPostDoc> {

    private final CommunityPostIndexer communityPostIndexer;
    private final String indexName;

    public CommunityPostPopularityManager(
            ElasticsearchClient esClient,
            @Qualifier("esObjectMapper") ObjectMapper esObjectMapper,
            CommunityPostIndexer communityPostIndexer,
            @Value("${elasticsearch.index.communitypost}") String indexName
    ) {
        super(esClient, esObjectMapper);
        this.communityPostIndexer = communityPostIndexer;
        this.indexName = indexName;
    }

    @Override
    protected String index() {
        return indexName;
    }

    @Override
    protected Class<CommunityPostDoc> docType() {
        return CommunityPostDoc.class;
    }

    @Override
    protected CommunityPostDoc updatePopularity(CommunityPostDoc doc, Popularity popularity) {
        return new CommunityPostDoc(
            doc.getDocId(),
            doc.getCommunityPostId(),
            doc.getUserId(),
            doc.getCategory(),
            doc.getTitle(),
            doc.getMarkdownBody(),
            doc.getCompany(),
            doc.getLocation(),
            doc.getLinkedJobId(),
            doc.getIsFromJobComment(),

            // Popularity 업데이트
            popularity.getViewCount(),
            popularity.getCommentCount(),
            popularity.getLikeCount(),

            doc.getDeleted(),
            doc.getCreatedAt(),
            doc.getUpdatedAt()
        );
    }

    @Override
    protected DocIndexer docIndexer() {
        return communityPostIndexer;
    }
}
