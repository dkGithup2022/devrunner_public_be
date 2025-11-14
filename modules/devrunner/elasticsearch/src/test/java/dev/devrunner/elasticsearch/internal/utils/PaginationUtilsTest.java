package dev.devrunner.elasticsearch.internal.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PaginationUtils 테스트
 *
 * ES 클러스터 없이 페이지네이션 로직을 검증
 */
@DisplayName("PaginationUtils 테스트")
class PaginationUtilsTest {

    @Nested
    @DisplayName("calculatePaginationInfo 테스트")
    class CalculatePaginationInfoTest {

        @Test
        @DisplayName("from과 to가 모두 제공된 경우")
        void calculatePaginationInfo_withBothFromAndTo_returnsCorrectInfo() {
            // Given
            Integer from = 10;
            Integer to = 30;
            int defaultPageSize = 20;

            // When
            var result = PaginationUtils.calculatePaginationInfo(from, to, defaultPageSize);

            // Then
            assertThat(result.from()).isEqualTo(10);
            assertThat(result.to()).isEqualTo(30);
            assertThat(result.requestedSize()).isEqualTo(20); // to - from
            assertThat(result.searchSize()).isEqualTo(21);    // requestedSize + 1 (for hasNext)
        }

        @Test
        @DisplayName("from만 제공되고 to가 null인 경우, defaultPageSize를 사용")
        void calculatePaginationInfo_withOnlyFrom_usesDefaultPageSize() {
            // Given
            Integer from = 5;
            Integer to = null;
            int defaultPageSize = 30;

            // When
            var result = PaginationUtils.calculatePaginationInfo(from, to, defaultPageSize);

            // Then
            assertThat(result.from()).isEqualTo(5);
            assertThat(result.to()).isEqualTo(35);            // from + defaultPageSize
            assertThat(result.requestedSize()).isEqualTo(30); // defaultPageSize
            assertThat(result.searchSize()).isEqualTo(31);    // requestedSize + 1
        }

        @Test
        @DisplayName("from과 to가 모두 null인 경우, 0부터 defaultPageSize")
        void calculatePaginationInfo_withBothNull_startsFromZero() {
            // Given
            Integer from = null;
            Integer to = null;
            int defaultPageSize = 20;

            // When
            var result = PaginationUtils.calculatePaginationInfo(from, to, defaultPageSize);

            // Then
            assertThat(result.from()).isEqualTo(0);
            assertThat(result.to()).isEqualTo(20);            // 0 + defaultPageSize
            assertThat(result.requestedSize()).isEqualTo(20); // defaultPageSize
            assertThat(result.searchSize()).isEqualTo(21);    // requestedSize + 1
        }

        @Test
        @DisplayName("from이 0인 경우, 첫 페이지 조회")
        void calculatePaginationInfo_withFromZero_returnsFirstPage() {
            // Given
            Integer from = 0;
            Integer to = 10;
            int defaultPageSize = 30;

            // When
            var result = PaginationUtils.calculatePaginationInfo(from, to, defaultPageSize);

            // Then
            assertThat(result.from()).isEqualTo(0);
            assertThat(result.to()).isEqualTo(10);
            assertThat(result.requestedSize()).isEqualTo(10);
            assertThat(result.searchSize()).isEqualTo(11);
        }

        @Test
        @DisplayName("from과 to가 동일한 경우, requestedSize는 0")
        void calculatePaginationInfo_withSameFromAndTo_returnsZeroSize() {
            // Given
            Integer from = 10;
            Integer to = 10;
            int defaultPageSize = 20;

            // When
            var result = PaginationUtils.calculatePaginationInfo(from, to, defaultPageSize);

            // Then
            assertThat(result.from()).isEqualTo(10);
            assertThat(result.to()).isEqualTo(10);
            assertThat(result.requestedSize()).isEqualTo(0);
            assertThat(result.searchSize()).isEqualTo(1); // 0 + 1
        }

        @Test
        @DisplayName("큰 페이지 번호에서도 정확히 계산")
        void calculatePaginationInfo_withLargeNumbers_calculatesCorrectly() {
            // Given
            Integer from = 1000;
            Integer to = 1050;
            int defaultPageSize = 30;

            // When
            var result = PaginationUtils.calculatePaginationInfo(from, to, defaultPageSize);

            // Then
            assertThat(result.from()).isEqualTo(1000);
            assertThat(result.to()).isEqualTo(1050);
            assertThat(result.requestedSize()).isEqualTo(50);
            assertThat(result.searchSize()).isEqualTo(51);
        }
    }

    @Nested
    @DisplayName("paginate 테스트")
    class PaginateTest {

        @Test
        @DisplayName("검색 결과가 requestedSize보다 많은 경우, hasNext는 true")
        void paginate_withMoreResults_returnsHasNextTrue() {
            // Given
            List<String> searchResults = List.of("a", "b", "c", "d", "e", "f");
            int requestedSize = 5;

            // When
            var result = PaginationUtils.paginate(searchResults, requestedSize);

            // Then
            assertThat(result.data()).hasSize(5);
            assertThat(result.data()).containsExactly("a", "b", "c", "d", "e");
            assertThat(result.hasNext()).isTrue(); // 6개 중 5개만 반환, 다음 페이지 존재
        }

        @Test
        @DisplayName("검색 결과가 정확히 requestedSize인 경우, hasNext는 false")
        void paginate_withExactSize_returnsHasNextFalse() {
            // Given
            List<String> searchResults = List.of("a", "b", "c", "d", "e");
            int requestedSize = 5;

            // When
            var result = PaginationUtils.paginate(searchResults, requestedSize);

            // Then
            assertThat(result.data()).hasSize(5);
            assertThat(result.data()).containsExactly("a", "b", "c", "d", "e");
            assertThat(result.hasNext()).isFalse(); // 정확히 5개, 다음 페이지 없음
        }

        @Test
        @DisplayName("검색 결과가 requestedSize보다 적은 경우, hasNext는 false")
        void paginate_withLessResults_returnsHasNextFalse() {
            // Given
            List<String> searchResults = List.of("a", "b", "c");
            int requestedSize = 5;

            // When
            var result = PaginationUtils.paginate(searchResults, requestedSize);

            // Then
            assertThat(result.data()).hasSize(3);
            assertThat(result.data()).containsExactly("a", "b", "c");
            assertThat(result.hasNext()).isFalse(); // 3개만 존재, 다음 페이지 없음
        }

        @Test
        @DisplayName("검색 결과가 비어있는 경우, 빈 리스트와 hasNext false 반환")
        void paginate_withEmptyResults_returnsEmptyWithHasNextFalse() {
            // Given
            List<String> searchResults = List.of();
            int requestedSize = 10;

            // When
            var result = PaginationUtils.paginate(searchResults, requestedSize);

            // Then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("requestedSize가 0인 경우, 빈 리스트 반환")
        void paginate_withZeroRequestedSize_returnsEmpty() {
            // Given
            List<String> searchResults = List.of("a", "b", "c");
            int requestedSize = 0;

            // When
            var result = PaginationUtils.paginate(searchResults, requestedSize);

            // Then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isTrue(); // 데이터가 있으므로 hasNext는 true
        }

        @Test
        @DisplayName("검색 결과가 requestedSize + 1개인 경우 (hasNext 경계 케이스)")
        void paginate_withExactlyOnMoreThanRequested_handlesCorrectly() {
            // Given - ES에서 requestedSize + 1개를 조회했고, 정확히 그만큼 반환
            List<Integer> searchResults = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
            int requestedSize = 10;

            // When
            var result = PaginationUtils.paginate(searchResults, requestedSize);

            // Then
            assertThat(result.data()).hasSize(10);
            assertThat(result.data()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            assertThat(result.hasNext()).isTrue(); // 11개 중 10개만 반환, 다음 페이지 존재
        }
    }

    @Nested
    @DisplayName("calculatePaginationInfo와 paginate 통합 시나리오")
    class IntegrationTest {

        @Test
        @DisplayName("첫 페이지 조회 시나리오 (from=null, to=null)")
        void firstPageScenario() {
            // Given - API에서 from=null, to=null로 요청
            int defaultPageSize = 10;
            var paginationInfo = PaginationUtils.calculatePaginationInfo(null, null, defaultPageSize);

            // ES는 searchSize(11)개를 조회
            List<String> esResults = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");

            // When
            var result = PaginationUtils.paginate(esResults, paginationInfo.requestedSize());

            // Then
            assertThat(result.data()).hasSize(10);
            assertThat(result.hasNext()).isTrue(); // 다음 페이지 존재
        }

        @Test
        @DisplayName("마지막 페이지 조회 시나리오 (다음 페이지 없음)")
        void lastPageScenario() {
            // Given - API에서 from=20, to=30으로 요청
            int defaultPageSize = 30;
            var paginationInfo = PaginationUtils.calculatePaginationInfo(20, 30, defaultPageSize);

            // ES는 searchSize(11)개를 요청했지만 5개만 반환 (마지막 페이지)
            List<String> esResults = List.of("21", "22", "23", "24", "25");

            // When
            var result = PaginationUtils.paginate(esResults, paginationInfo.requestedSize());

            // Then
            assertThat(result.data()).hasSize(5);
            assertThat(result.hasNext()).isFalse(); // 다음 페이지 없음
        }

        @Test
        @DisplayName("중간 페이지 조회 시나리오")
        void middlePageScenario() {
            // Given - API에서 from=10, to=20으로 요청
            int defaultPageSize = 30;
            var paginationInfo = PaginationUtils.calculatePaginationInfo(10, 20, defaultPageSize);

            // ES는 searchSize(11)개를 조회
            List<Integer> esResults = List.of(11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21);

            // When
            var result = PaginationUtils.paginate(esResults, paginationInfo.requestedSize());

            // Then
            assertThat(result.data()).hasSize(10);
            assertThat(result.data()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
            assertThat(result.hasNext()).isTrue();
        }
    }
}
