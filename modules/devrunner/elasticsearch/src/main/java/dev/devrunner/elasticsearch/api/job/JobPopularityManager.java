package dev.devrunner.elasticsearch.api.job;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.elasticsearch.api.AbstractDocPopularityManager;
import dev.devrunner.elasticsearch.document.JobDoc;
import dev.devrunner.elasticsearch.internal.indexer.DocIndexer;
import dev.devrunner.model.common.Popularity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JobPopularityManager extends AbstractDocPopularityManager<JobDoc> {

    private final JobIndexer jobIndexer;
    private final String indexName;

    public JobPopularityManager(
            ElasticsearchClient esClient,
            @Qualifier("esObjectMapper") ObjectMapper esObjectMapper,
            JobIndexer jobIndexer,
            @Value("${elasticsearch.index.job}") String indexName
    ) {
        super(esClient, esObjectMapper);
        this.jobIndexer = jobIndexer;
        this.indexName = indexName;
    }

    @Override
    protected String index() {
        return indexName;
    }

    @Override
    protected Class<JobDoc> docType() {
        return JobDoc.class;
    }

    @Override
    protected JobDoc updatePopularity(JobDoc doc, Popularity popularity) {
        return new JobDoc(
            doc.getDocId(),
            doc.getJobId(),
            doc.getUrl(),
            doc.getCompany(),
            doc.getTitle(),
            doc.getOrganization(),
            doc.getOneLineSummary(),
            doc.getMinYears(),
            doc.getMaxYears(),
            doc.getExperienceRequired(),
            doc.getCareerLevel(),
            doc.getEmploymentType(),
            doc.getPositionCategory(),
            doc.getRemotePolicy(),
            doc.getTechCategories(),
            doc.getStartedAt(),
            doc.getEndedAt(),
            doc.getIsOpenEnded(),
            doc.getIsClosed(),
            doc.getLocations(),
            doc.getFullDescription(),
            doc.getHasAssignment(),
            doc.getHasCodingTest(),
            doc.getHasLiveCoding(),
            doc.getInterviewCount(),
            doc.getInterviewDays(),
            doc.getCompensationMinBasePay(),
            doc.getCompensationMaxBasePay(),
            doc.getCompensationCurrency(),
            doc.getCompensationUnit(),
            doc.getCompensationHasStockOption(),

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
        return jobIndexer;
    }
}
