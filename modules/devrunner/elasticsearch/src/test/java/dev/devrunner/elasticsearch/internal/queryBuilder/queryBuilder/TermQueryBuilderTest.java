package dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import dev.devrunner.elasticsearch.internal.queryBuilder.BoolType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.devrunner.elasticsearch.testutil.ElasticsearchQueryTestHelper.printQuery;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * TermQueryBuilder 테스트
 *
 * Term 쿼리는 정확한 값 매칭에 사용 (keyword 필드)
 * - company, career_level, employment_type 등
 */
@DisplayName("TermQueryBuilder 테스트")
class TermQueryBuilderTest {

    @Test
    @DisplayName("문자열 값으로 term 쿼리 생성")
    void build_withStringValue_createsTermQuery() {
        // Given
        TermQueryBuilder builder = new TermQueryBuilder(BoolType.FILTER);

        // When
        Query query = builder.build("company", "META");

        // Then - 첫 실행 시 비어있음, 출력된 JSON을 복사해서 채우기
        String expectedJson = """
                {
                  "term" : {
                    "company" : {
                      "value" : "META"
                    }
                  }
                }
        """;

        printQuery("build_withStringValue_createsTermQuery", query);
    }

    @Test
    @DisplayName("Boolean 필드로 term 쿼리 생성")
    void build_withBooleanField_createsTermQuery() {
        // Given
        TermQueryBuilder builder = new TermQueryBuilder(BoolType.MUST);

        // When
        Query query = builder.build("deleted", "false");

        // Then
        printQuery("build_withBooleanField_createsTermQuery", query);
    }

    @Test
    @DisplayName("Enum 값으로 term 쿼리 생성")
    void build_withEnumValue_createsTermQuery() {
        // Given
        TermQueryBuilder builder = new TermQueryBuilder(BoolType.FILTER);

        // When
        Query query = builder.build("career_level", "ENTRY");

        // Then
        printQuery("build_withEnumValue_createsTermQuery", query);
    }

    @Test
    @DisplayName("FILTER BoolType 반환 확인")
    void boolType_withFilter_returnsFilter() {
        // Given
        TermQueryBuilder builder = new TermQueryBuilder(BoolType.FILTER);

        // When & Then
        assertThat(builder.boolType()).isEqualTo(BoolType.FILTER);
    }

    @Test
    @DisplayName("MUST BoolType 반환 확인")
    void boolType_withMust_returnsMust() {
        // Given
        TermQueryBuilder builder = new TermQueryBuilder(BoolType.MUST);

        // When & Then
        assertThat(builder.boolType()).isEqualTo(BoolType.MUST);
    }

    @Test
    @DisplayName("SHOULD BoolType 반환 확인")
    void boolType_withShould_returnsShould() {
        // Given
        TermQueryBuilder builder = new TermQueryBuilder(BoolType.SHOULD);

        // When & Then
        assertThat(builder.boolType()).isEqualTo(BoolType.SHOULD);
    }
}
