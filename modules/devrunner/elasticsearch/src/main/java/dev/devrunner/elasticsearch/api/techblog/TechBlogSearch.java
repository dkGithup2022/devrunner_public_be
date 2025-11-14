package dev.devrunner.elasticsearch.api.techblog;

import dev.devrunner.elasticsearch.document.TechBlogDoc;
import dev.devrunner.elasticsearch.document.fieldSpec.techblog.TechBlogIndexField;
import dev.devrunner.elasticsearch.internal.query.techblog.TechBlogIndexQueryBuilderRegistry;
import dev.devrunner.elasticsearch.internal.query.techblog.TechBlogIndexRangeQueryBuilderRegistry;
import dev.devrunner.elasticsearch.internal.queryBuilder.GenericSearchQueryBuilder;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchCommand;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import dev.devrunner.elasticsearch.internal.queryBuilder.SortOption;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryExecutor.SearchQueryExecutor;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryExecutor.SearchResult;
import dev.devrunner.elasticsearch.internal.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TechBlogSearch {

    @Value("${elasticsearch.index.techblog}")
    private String TECH_BLOG_INDEX;

    private final SearchQueryExecutor executor;
    private static final Integer DEFAULT_PAGE_SIZE = 30;

    public TechBlogSearchResult search(SearchCommand<TechBlogIndexField> command) {
        // deleted=false 조건 자동 추가
        var commandWithDeleted = ensureDeletedFalseCondition(command);

        var q = GenericSearchQueryBuilder.build(commandWithDeleted,
                TechBlogIndexQueryBuilderRegistry.LOOKUP,
                TechBlogIndexRangeQueryBuilderRegistry.LOOKUP);

        var pagination = PaginationUtils.calculatePaginationInfo(commandWithDeleted.from(), commandWithDeleted.to(), DEFAULT_PAGE_SIZE);

        SearchResult<TechBlogDoc> searchResult;

        if (hasMatchQuery(commandWithDeleted)) {
            // match 쿼리가 있으면 relevance score로 정렬
            searchResult = executor.search(TECH_BLOG_INDEX, q, pagination.from(), pagination.searchSize(), TechBlogDoc.class);
        } else {
            // match 쿼리가 없으면 created_at desc로 정렬
            searchResult = executor.searchWithSort(TECH_BLOG_INDEX, q, pagination.from(), pagination.searchSize(),
                    SortOption.desc("created_at"), TechBlogDoc.class);
        }

        var result = PaginationUtils.paginate(searchResult.docs(), pagination.requestedSize());
        return new TechBlogSearchResult(result.data(), result.hasNext(), searchResult.totalHits());
    }

    private boolean hasMatchQuery(SearchCommand<TechBlogIndexField> command) {
        return command.conditions().stream()
                .anyMatch(element -> element.getField() == TechBlogIndexField.SEARCH_WORD
                        || element.getField() == TechBlogIndexField.TITLE
                || element.getField() == TechBlogIndexField.SUMMARY );
    }


    public TechBlogSearchResult searchByVector(List<Float> queryVector, SearchCommand<TechBlogIndexField> command, int size) {
        // deleted=false 조건 자동 추가
        var commandWithDeleted = ensureDeletedFalseCondition(command);

        // SearchCommand의 조건들로 필터 쿼리 생성
        var filterQuery = GenericSearchQueryBuilder.build(commandWithDeleted,
                TechBlogIndexQueryBuilderRegistry.LOOKUP,
                TechBlogIndexRangeQueryBuilderRegistry.LOOKUP);

        // KNN 검색 수행 (페이지네이션 없음)
        var docs = executor.searchByVector(TECH_BLOG_INDEX, queryVector, filterQuery, size, TechBlogDoc.class);

        return new TechBlogSearchResult(docs, false); // KNN은 hasNext 없음
    }

    private SearchCommand<TechBlogIndexField> ensureDeletedFalseCondition(SearchCommand<TechBlogIndexField> command) {
        // 이미 DELETED 조건이 있는지 확인
        boolean hasDeletedCondition = command.conditions().stream()
                .anyMatch(element -> element.getField() == TechBlogIndexField.DELETED);

        if (hasDeletedCondition) {
            return command; // 이미 deleted 조건이 있으면 그대로 반환
        }

        // deleted=false 조건 추가
        List<SearchElement<TechBlogIndexField>> newConditions = new ArrayList<>(command.conditions());
        newConditions.add(new SearchElement<>(TechBlogIndexField.DELETED, false));

        return new SearchCommand<>(newConditions, command.from(), command.to());
    }

    public TechBlogDoc getById(String docId) {
        return executor.getById(TECH_BLOG_INDEX, docId, TechBlogDoc.class);
    }
}
