package dev.devrunner.elasticsearch.api.techblog;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.elasticsearch.api.AbstractDocPopularityManager;
import dev.devrunner.elasticsearch.document.TechBlogDoc;
import dev.devrunner.elasticsearch.internal.indexer.DocIndexer;
import dev.devrunner.model.common.Popularity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TechBlogPopularityManager extends AbstractDocPopularityManager<TechBlogDoc> {

    private final TechBlogIndexer techBlogIndexer;
    private final String indexName;

    public TechBlogPopularityManager(
            ElasticsearchClient esClient,
            @Qualifier("esObjectMapper") ObjectMapper esObjectMapper,
            TechBlogIndexer techBlogIndexer,
            @Value("${elasticsearch.index.techblog}") String indexName
    ) {
        super(esClient, esObjectMapper);
        this.techBlogIndexer = techBlogIndexer;
        this.indexName = indexName;
    }

    @Override
    protected String index() {
        return indexName;
    }

    @Override
    protected Class<TechBlogDoc> docType() {
        return TechBlogDoc.class;
    }

    @Override
    protected TechBlogDoc updatePopularity(TechBlogDoc doc, Popularity popularity) {
        return TechBlogDoc.of(
            doc.getDocId(),
            doc.getTechBlogId(),
            doc.getUrl(),
            doc.getCompany(),
            doc.getTitle(),
            doc.getOneLiner(),
            doc.getSummary(),
            doc.getKoreanSummary(),
            doc.getMarkdownBody(),
            doc.getThumbnailUrl(),
            doc.getTechCategories(),
            doc.getOriginalUrl(),

            // Popularity 업데이트
            popularity.getViewCount(),
            popularity.getCommentCount(),
            popularity.getLikeCount(),

            doc.getDeleted(),
            doc.getCreatedAt(),
            doc.getUpdatedAt(),
            doc.getVector()
        );
    }

    @Override
    protected DocIndexer docIndexer() {
        return techBlogIndexer;
    }
}
