package dev.devrunner.elasticsearch.internal.queryBuilder;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import dev.devrunner.elasticsearch.document.fieldSpec.DocFieldName;
import dev.devrunner.elasticsearch.document.fieldSpec.job.JobIndexField;
import dev.devrunner.elasticsearch.exception.SearchFieldNotFoundException;
import dev.devrunner.elasticsearch.internal.query.job.JobIndexQueryBuilderRegistry;
import dev.devrunner.elasticsearch.internal.query.job.JobIndexRangeQueryBuilderRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static dev.devrunner.elasticsearch.testutil.ElasticsearchQueryTestHelper.printQuery;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * GenericSearchQueryBuilder 통합 테스트
 *
 * SearchCommand와 Registry를 조합하여 실제 쿼리 생성 검증
 */
@DisplayName("GenericSearchQueryBuilder 통합 테스트")
class GenericSearchQueryBuilderTest {

    @Test
    @DisplayName("여러 조건 혼합 시 BoolQuery 생성 - TERM + MATCH + RANGE")
    void build_withMixedConditions_createsBoolQuery() {
        // Given
        List<SearchElement<JobIndexField>> conditions = List.of(
            new SearchElement<>(JobIndexField.COMPANY, "META"),              // TERM - FILTER
            new SearchElement<>(JobIndexField.TITLE, "백엔드"),                // MATCH - SHOULD
            new SearchElement<>(JobIndexField.MIN_YEARS, 0, 3),              // RANGE - FILTER
            new SearchElement<>(JobIndexField.DELETED, false)                // TERM - MUST
        );

        SearchCommand<JobIndexField> command = SearchCommand.of(conditions, 0, 20);

        // When
        Query query = GenericSearchQueryBuilder.build(
            command,
            JobIndexQueryBuilderRegistry.LOOKUP,
            JobIndexRangeQueryBuilderRegistry.LOOKUP
        );

        // Then - 첫 실행 시 비어있음, 출력된 JSON을 복사해서 채우기
        String expectedJson = """
                 {
                    "bool" : {
                      "filter" : [ {
                        "term" : {
                          "company" : {
                            "value" : "META"
                          }
                        }
                      }, {
                        "range" : {
                          "min_years" : {
                            "gte" : "0",
                            "lte" : "3"
                          }
                        }
                      } ],
                      "minimum_should_match" : "1",
                      "must" : [ {
                        "term" : {
                          "deleted" : {
                            "value" : "false"
                          }
                        }
                      } ],
                      "should" : [ {
                        "match" : {
                          "title" : {
                            "query" : "백엔드"
                          }
                        }
                      } ]
                    }
                    }
        """;

        printQuery("build_withMixedConditions_createsBoolQuery", query);
    }

    @Test
    @DisplayName("등록되지 않은 필드 사용 시 SearchFieldNotFoundException 발생")
    void build_withUnregisteredField_throwsSearchFieldNotFoundException() {
        // Given - DOC_ID는 Registry에 등록되지 않음
        List<SearchElement<JobIndexField>> conditions = List.of(
            new SearchElement<>(JobIndexField.DOC_ID, "job_123")
        );
        SearchCommand<JobIndexField> command = SearchCommand.of(conditions, 0, 10);

        // When & Then
        assertThatThrownBy(() ->
            GenericSearchQueryBuilder.build(
                command,
                field -> Optional.empty(), // 등록되지 않은 필드
                field -> Optional.empty()
            )
        )
        .isInstanceOf(SearchFieldNotFoundException.class)
        .hasMessageContaining("No query builder found for field");
    }
}
