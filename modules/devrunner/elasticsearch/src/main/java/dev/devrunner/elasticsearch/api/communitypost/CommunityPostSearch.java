package dev.devrunner.elasticsearch.api.communitypost;

import dev.devrunner.elasticsearch.document.CommunityPostDoc;
import dev.devrunner.elasticsearch.document.JobDoc;
import dev.devrunner.elasticsearch.document.fieldSpec.communitypost.CommunityPostIndexField;
import dev.devrunner.elasticsearch.internal.query.communitypost.CommunityPostIndexQueryBuilderRegistry;
import dev.devrunner.elasticsearch.internal.query.communitypost.CommunityPostIndexRangeQueryBuilderRegistry;
import dev.devrunner.elasticsearch.internal.queryBuilder.GenericSearchQueryBuilder;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchCommand;
import dev.devrunner.elasticsearch.internal.queryBuilder.SortOption;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryExecutor.SearchQueryExecutor;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryExecutor.SearchResult;
import dev.devrunner.elasticsearch.internal.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommunityPostSearch {

    @Value("${elasticsearch.index.communitypost}")
    private String COMMUNITY_POST_INDEX;

    private final SearchQueryExecutor executor;
    private static final Integer DEFAULT_PAGE_SIZE = 30;

    public CommunityPostSearchResult search(SearchCommand<CommunityPostIndexField> command) {
        var q = GenericSearchQueryBuilder.build(command,
                CommunityPostIndexQueryBuilderRegistry.LOOKUP,
                CommunityPostIndexRangeQueryBuilderRegistry.LOOKUP);

        var pagination = PaginationUtils.calculatePaginationInfo(command.from(), command.to(), DEFAULT_PAGE_SIZE);

        SearchResult<CommunityPostDoc> searchResult;

        if (hasMatchQuery(command)) {
            // match 쿼리가 있으면 relevance score로 정렬
            searchResult = executor.search(COMMUNITY_POST_INDEX, q, pagination.from(), pagination.searchSize(), CommunityPostDoc.class);
        } else {
            // match 쿼리가 없으면 created_at desc로 정렬
            searchResult = executor.searchWithSort(COMMUNITY_POST_INDEX, q, pagination.from(), pagination.searchSize(),
                    SortOption.desc("created_at"), CommunityPostDoc.class);
        }

        var result = PaginationUtils.paginate(searchResult.docs(), pagination.requestedSize());
        return new CommunityPostSearchResult(result.data(), result.hasNext(), searchResult.totalHits());
    }

    private boolean hasMatchQuery(SearchCommand<CommunityPostIndexField> command) {
        return command.conditions().stream()
                .anyMatch(element -> element.getField() == CommunityPostIndexField.TITLE
                        || element.getField() == CommunityPostIndexField.MARKDOWN_BODY);
    }
}
