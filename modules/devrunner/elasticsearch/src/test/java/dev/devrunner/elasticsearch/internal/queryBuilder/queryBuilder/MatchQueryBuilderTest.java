package dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import dev.devrunner.elasticsearch.internal.queryBuilder.BoolType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.devrunner.elasticsearch.testutil.ElasticsearchQueryTestHelper.printQuery;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * MatchQueryBuilder 테스트
 *
 * Match 쿼리는 전문 검색(full-text search)에 사용 (text 필드)
 * - title, full_description 등
 */
@DisplayName("MatchQueryBuilder 테스트")
class MatchQueryBuilderTest {

    @Test
    @DisplayName("한글 텍스트로 match 쿼리 생성")
    void build_withKoreanText_createsMatchQuery() {
        // Given
        MatchQueryBuilder builder = new MatchQueryBuilder(BoolType.SHOULD);

        // When
        Query query = builder.build("title", "백엔드 개발자");

        // Then - 첫 실행 시 비어있음, 출력된 JSON을 복사해서 채우기
        String expectedJson = """
                {
                  "match" : {
                    "title" : {
                      "query" : "백엔드 개발자"
                    }
                  }
                }
        """;

        printQuery("build_withKoreanText_createsMatchQuery", query);
    }

    @Test
    @DisplayName("영문 텍스트로 match 쿼리 생성")
    void build_withEnglishText_createsMatchQuery() {
        // Given
        MatchQueryBuilder builder = new MatchQueryBuilder(BoolType.SHOULD);

        // When
        Query query = builder.build("full_description", "Spring Boot Java");

        // Then
        printQuery("build_withEnglishText_createsMatchQuery", query);
    }

    @Test
    @DisplayName("한글+영문 혼합 텍스트로 match 쿼리 생성")
    void build_withMixedText_createsMatchQuery() {
        // Given
        MatchQueryBuilder builder = new MatchQueryBuilder(BoolType.SHOULD);

        // When
        Query query = builder.build("title", "Senior Backend Developer 시니어");

        // Then
        printQuery("build_withMixedText_createsMatchQuery", query);
    }

    @Test
    @DisplayName("특수문자 포함 텍스트로 match 쿼리 생성")
    void build_withSpecialCharacters_createsMatchQuery() {
        // Given
        MatchQueryBuilder builder = new MatchQueryBuilder(BoolType.SHOULD);

        // When
        Query query = builder.build("full_description", "C++ / Python (Django)");

        // Then
        printQuery("build_withSpecialCharacters_createsMatchQuery", query);
    }

    @Test
    @DisplayName("SHOULD BoolType 반환 확인")
    void boolType_withShould_returnsShould() {
        // Given
        MatchQueryBuilder builder = new MatchQueryBuilder(BoolType.SHOULD);

        // When & Then
        assertThat(builder.boolType()).isEqualTo(BoolType.SHOULD);
    }

    @Test
    @DisplayName("MUST BoolType 반환 확인")
    void boolType_withMust_returnsMust() {
        // Given
        MatchQueryBuilder builder = new MatchQueryBuilder(BoolType.MUST);

        // When & Then
        assertThat(builder.boolType()).isEqualTo(BoolType.MUST);
    }
}
