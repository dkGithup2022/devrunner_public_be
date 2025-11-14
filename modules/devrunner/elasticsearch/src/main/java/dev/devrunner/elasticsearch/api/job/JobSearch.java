package dev.devrunner.elasticsearch.api.job;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import dev.devrunner.elasticsearch.api.job.boost.JobDocBoostQueryHelper;
import dev.devrunner.elasticsearch.document.JobDoc;
import dev.devrunner.elasticsearch.document.fieldSpec.job.JobIndexField;
import dev.devrunner.elasticsearch.internal.query.job.JobIndexQueryBuilderRegistry;
import dev.devrunner.elasticsearch.internal.query.job.JobIndexRangeQueryBuilderRegistry;
import dev.devrunner.elasticsearch.internal.queryBuilder.GenericSearchQueryBuilder;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchCommand;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import dev.devrunner.elasticsearch.internal.queryBuilder.SortOption;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryExecutor.SearchQueryExecutor;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryExecutor.SearchResult;
import dev.devrunner.elasticsearch.internal.utils.PaginationUtils;
import dev.devrunner.elasticsearch.internal.utils.QueryHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobSearch {

    @Value("${elasticsearch.index.job}")
    private String JOB_INDEX;

    private final SearchQueryExecutor executor;
    private final JobDocBoostQueryHelper jobDocBoostQueryHelper;
    private static final Integer DEFAULT_PAGE_SIZE = 30;

    public JobSearchResult search(SearchCommand<JobIndexField> command) {
        // deleted=false 조건 자동 추가
        var commandWithDeleted = ensureDeletedFalseCondition(command);

        var q = GenericSearchQueryBuilder.build(commandWithDeleted,
                JobIndexQueryBuilderRegistry.LOOKUP,
                JobIndexRangeQueryBuilderRegistry.LOOKUP);

        var pagination = PaginationUtils.calculatePaginationInfo(commandWithDeleted.from(), commandWithDeleted.to(), DEFAULT_PAGE_SIZE);

        // 부스트 should 생성
        List<Query> boostShoulds = jobDocBoostQueryHelper.hasSearchWord(commandWithDeleted)
                ? jobDocBoostQueryHelper.buildBoostQueries(commandWithDeleted)
                : List.of();

        log.info("Before merge query: {}", q);
        // base에 should 병합
        var finalQuery = QueryHelper.mergeShoulds(q, boostShoulds);
        log.info("After merge query: {}", finalQuery);

        SearchResult<JobDoc> searchResult;

        if (hasMatchQuery(commandWithDeleted)) {
            // match 쿼리가 있으면 relevance score로 정렬
            searchResult = executor.search(JOB_INDEX, finalQuery, pagination.from(), pagination.searchSize(), JobDoc.class);
        } else {
            // match 쿼리가 없으면 created_at desc로 정렬
            searchResult = executor.searchWithSort(JOB_INDEX, finalQuery, pagination.from(), pagination.searchSize(),
                    SortOption.desc("created_at"), JobDoc.class);
        }

        var result = PaginationUtils.paginate(searchResult.docs(), pagination.requestedSize());
        return new JobSearchResult(result.data(), result.hasNext(), searchResult.totalHits());
    }

    private boolean hasMatchQuery(SearchCommand<JobIndexField> command) {
        return command.conditions().stream()
                .anyMatch(element -> element.getField() == JobIndexField.TITLE
                        || element.getField() == JobIndexField.ONE_LINE_SUMMARY
                        || element.getField() == JobIndexField.FULL_DESCRIPTION);
    }


    public JobSearchResult searchByVector(List<Float> queryVector, SearchCommand<JobIndexField> command, int size) {
        // deleted=false 조건 자동 추가
        var commandWithDeleted = ensureDeletedFalseCondition(command);

        // SearchCommand의 조건들로 필터 쿼리 생성
        var filterQuery = GenericSearchQueryBuilder.build(commandWithDeleted,
                JobIndexQueryBuilderRegistry.LOOKUP,
                JobIndexRangeQueryBuilderRegistry.LOOKUP);

        // KNN 검색 수행 (페이지네이션 없음)
        var docs = executor.searchByVector(JOB_INDEX, queryVector, filterQuery, size, JobDoc.class);

        // 벡터는 항상 정해진크기 응답
        // KNN은 hasNext 없음
        return new JobSearchResult(docs, false, docs.size());
    }

    private SearchCommand<JobIndexField> ensureDeletedFalseCondition(SearchCommand<JobIndexField> command) {
        // 이미 DELETED 조건이 있는지 확인
        boolean hasDeletedCondition = command.conditions().stream()
                .anyMatch(element -> element.getField() == JobIndexField.DELETED);

        if (hasDeletedCondition) {
            return command; // 이미 deleted 조건이 있으면 그대로 반환
        }

        // deleted=false 조건 추가
        List<SearchElement<JobIndexField>> newConditions = new ArrayList<>(command.conditions());
        newConditions.add(new SearchElement<>(JobIndexField.DELETED, false));

        return new SearchCommand<>(newConditions, command.from(), command.to());
    }

    public JobDoc getById(String docId) {
        return executor.getById(JOB_INDEX, docId, JobDoc.class);
    }
}
