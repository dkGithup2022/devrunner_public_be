package dev.devrunner.elasticsearch.mapper;

import dev.devrunner.elasticsearch.document.TechBlogDoc;
import dev.devrunner.elasticsearch.vector.E5Vectorizer;
import dev.devrunner.elasticsearch.vector.VectorizeException;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TechCategory;
import dev.devrunner.model.techblog.TechBlog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * TechBlogDocMapper 테스트
 * <p>
 * Mock E5Vectorizer를 사용하여 ES 클러스터 없이 매핑 로직 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TechBlogDocMapper 테스트")
class TechBlogDocMapperTest {

    @Mock
    private E5Vectorizer mockVectorizer;

    private TechBlogDocMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TechBlogDocMapper(mockVectorizer);
    }

    @Nested
    @DisplayName("정상 변환 테스트")
    class SuccessfulConversionTest {

        @Test
        @DisplayName("모든 필드가 채워진 TechBlog을 TechBlogDoc으로 변환")
        void newDoc_withFullTechBlog_convertsCorrectly() {
            // Given
            Instant now = Instant.now();
            when(mockVectorizer.vectorize(anyString())).thenReturn(List.of(0.1f, 0.2f, 0.3f, 0.4f, 0.5f));


            TechBlog techBlog = new TechBlog(
                    100L,
                    "https://example.com/blog/100",
                    "Google Engineering",
                    "How We Built YouTube Recommendations",
                    "Learn about our ML pipeline",
                    "We used TensorFlow and Kubernetes to build a scalable recommendation system...",
                    "# 유튜브 추천 서비스를 만드는 방법 ",
                    "# How We Built YouTube Recommendations\n\nFull markdown content here...",

                    "https://example.com/thumbnail.jpg",
                    List.of(TechCategory.PYTHON, TechCategory.K8S, TechCategory.MACHINE_LEARNING),
                    "https://engineering.google.com/blog/youtube-recommendations",
                    new Popularity(5000L, 120L, 800L, 0L),
                    false,
                    now,
                    now
            );

            // When
            TechBlogDoc doc = mapper.newDoc(techBlog);

            // Then
            assertThat(doc).isNotNull();
            assertThat(doc.getDocId()).isEqualTo("techblog_100");
            assertThat(doc.getTechBlogId()).isEqualTo(100L);
            assertThat(doc.getUrl()).isEqualTo("https://example.com/blog/100");
            assertThat(doc.getCompany()).isEqualTo("Google Engineering");
            assertThat(doc.getTitle()).isEqualTo("How We Built YouTube Recommendations");
            assertThat(doc.getOneLiner()).isEqualTo("Learn about our ML pipeline");
            assertThat(doc.getSummary()).isEqualTo("We used TensorFlow and Kubernetes to build a scalable recommendation system...");
            assertThat(doc.getMarkdownBody()).startsWith("# How We Built YouTube Recommendations");
            assertThat(doc.getThumbnailUrl()).isEqualTo("https://example.com/thumbnail.jpg");
            assertThat(doc.getTechCategories()).containsExactly("PYTHON", "K8S", "MACHINE_LEARNING");
            assertThat(doc.getOriginalUrl()).isEqualTo("https://engineering.google.com/blog/youtube-recommendations");

            // Popularity
            assertThat(doc.getPopularityViewCount()).isEqualTo(5000L);
            assertThat(doc.getPopularityCommentCount()).isEqualTo(120L);
            assertThat(doc.getPopularityLikeCount()).isEqualTo(800L);

            // Meta
            assertThat(doc.getDeleted()).isFalse();

            // Vector
            assertThat(doc.getVector()).containsExactly(0.1f, 0.2f, 0.3f, 0.4f, 0.5f);
        }

        @Test
        @DisplayName("필수 필드만 있는 TechBlog을 TechBlogDoc으로 변환 (null 처리)")
        void newDoc_withMinimalTechBlog_convertsWithNulls() {
            // Given
            when(mockVectorizer.vectorize(anyString())).thenReturn(List.of(0.1f, 0.2f, 0.3f, 0.4f, 0.5f));

            Instant now = Instant.now();
            TechBlog techBlog = new TechBlog(
                    200L,
                    "https://example.com/blog/200",
                    null, // company null
                    "Simple Blog Post",
                    null, // oneLiner null
                    "This is a summary", // summary 필수 (벡터화용)
                    "간단한 포스트",
                    "# Simple Blog Post",

                    null, // thumbnailUrl null
                    null, // techCategories null
                    null, // originalUrl null
                    null, // popularity null
                    false,
                    now,
                    now
            );

            // When
            TechBlogDoc doc = mapper.newDoc(techBlog);

            // Then
            assertThat(doc).isNotNull();
            assertThat(doc.getDocId()).isEqualTo("techblog_200");
            assertThat(doc.getTechBlogId()).isEqualTo(200L);

            // null 필드 확인
            assertThat(doc.getCompany()).isNull();
            assertThat(doc.getOneLiner()).isNull();
            assertThat(doc.getThumbnailUrl()).isNull();
            assertThat(doc.getTechCategories()).isNull();
            assertThat(doc.getOriginalUrl()).isNull();
            assertThat(doc.getPopularityViewCount()).isNull();
            assertThat(doc.getPopularityCommentCount()).isNull();
            assertThat(doc.getPopularityLikeCount()).isNull();

            // 필수 필드는 정상
            assertThat(doc.getSummary()).isEqualTo("This is a summary");
            assertThat(doc.getVector()).isNotNull();
        }

        @Test
        @DisplayName("TechCategory enum 리스트를 String 리스트로 변환")
        void newDoc_withTechCategories_convertsToStringList() {
            // Given
            when(mockVectorizer.vectorize(anyString())).thenReturn(List.of(0.1f, 0.2f, 0.3f, 0.4f, 0.5f));

            Instant now = Instant.now();
            TechBlog techBlog = new TechBlog(
                    1L, "url", null, "title", null, "summary", "요약", "body", null,
                    List.of(TechCategory.JAVA, TechCategory.SPRING, TechCategory.KAFKA),
                    null, null, false, now, now
            );

            // When
            TechBlogDoc doc = mapper.newDoc(techBlog);

            // Then
            assertThat(doc.getTechCategories())
                    .containsExactly("JAVA", "SPRING", "KAFKA");
        }

        @Test
        @DisplayName("빈 TechCategory 리스트도 정상 처리")
        void newDoc_withEmptyTechCategories_convertsToEmptyList() {
            // Given
            when(mockVectorizer.vectorize(anyString())).thenReturn(List.of(0.1f, 0.2f, 0.3f, 0.4f, 0.5f));

            Instant now = Instant.now();
            TechBlog techBlog = new TechBlog(
                    1L, "url", null, "title", null, "summary", "요약", "body", null,

                    List.of(), // 빈 리스트
                    null, null, false, now, now
            );

            // When
            TechBlogDoc doc = mapper.newDoc(techBlog);

            // Then
            assertThat(doc.getTechCategories()).isEmpty();
        }
    }

    @Nested
    @DisplayName("예외 상황 테스트")
    class ExceptionTest {

        @Test
        @DisplayName("TechBlog이 null이면 null 반환")
        void newDoc_withNullTechBlog_returnsNull() {
            // When
            TechBlogDoc doc = mapper.newDoc(null);

            // Then
            assertThat(doc).isNull();
        }

        @Test
        @DisplayName("summary가 null이면 VectorizeException 발생")
        void newDoc_withNullSummary_throwsVectorizeException() {
            // Given
            Instant now = Instant.now();
            TechBlog techBlog = new TechBlog(
                    1L, "url", null, "title", null,
                    null, // summary null ,
                    null,
                    "body", null, null, null, null, false, now, now
            );


            // When & Then
            assertThatThrownBy(() -> mapper.newDoc(techBlog))
                    .isInstanceOf(VectorizeException.class)
                    .hasMessageContaining("Cannot index TechBlog without summary")
                    .hasMessageContaining("techBlogId: 1");
        }

        @Test
        @DisplayName("vectorizer가 실패하여 null 반환하면 VectorizeException 발생")
        void newDoc_whenVectorizerReturnsNull_throwsVectorizeException() {
            // Given
            Instant now = Instant.now();
            TechBlog techBlog = new TechBlog(
                    999L, "url", null, "title", null, "summary", "서머리", "body", null,
                    null, null, null, false, now, now
            );

            // vectorizer가 예외적으로 null 반환
            when(mockVectorizer.vectorize("summary")).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> mapper.newDoc(techBlog))
                    .isInstanceOf(VectorizeException.class)
                    .hasMessageContaining("techBlogId: 999");
        }
    }

    @Nested
    @DisplayName("DocID 생성 테스트")
    class DocIdGenerationTest {

        @Test
        @DisplayName("techBlogId를 사용하여 'techblog_{techBlogId}' 형식의 docId 생성")
        void newDoc_generatesCorrectDocId() {
            // Given
            when(mockVectorizer.vectorize(anyString())).thenReturn(List.of(0.1f, 0.2f, 0.3f, 0.4f, 0.5f));

            Instant now = Instant.now();
            TechBlog techBlog1 = new TechBlog(
                    123L, "url", null, "title", null, "summary", "서머리ㅣ", "body", null,
                    null, null, null, false, now, now
            );

            TechBlog techBlog2 = new TechBlog(
                    999L, "url", null, "title", null, "summary", "서머리ㅣ","body", null,
                    null, null, null, false, now, now
            );

            // When
            TechBlogDoc doc1 = mapper.newDoc(techBlog1);
            TechBlogDoc doc2 = mapper.newDoc(techBlog2);

            // Then
            assertThat(doc1.getDocId()).isEqualTo("techblog_123");
            assertThat(doc2.getDocId()).isEqualTo("techblog_999");
        }
    }

    @Nested
    @DisplayName("Popularity Flattening 테스트")
    class PopularityFlatteningTest {

        @Test
        @DisplayName("Popularity 객체의 필드가 개별 필드로 flattening")
        void newDoc_flattensPopularityFields() {
            // Given
            when(mockVectorizer.vectorize(anyString())).thenReturn(List.of(0.1f, 0.2f, 0.3f, 0.4f, 0.5f));

            Instant now = Instant.now();
            TechBlog techBlog = new TechBlog(
                    1L, "url", null, "title", null, "summary", "서머리ㅣ","body", null,
                    null, null, new Popularity(10000L, 250L, 1500L, 0L),
                    false, now, now
            );

            // When
            TechBlogDoc doc = mapper.newDoc(techBlog);

            // Then
            assertThat(doc.getPopularityViewCount()).isEqualTo(10000L);
            assertThat(doc.getPopularityCommentCount()).isEqualTo(250L);
            assertThat(doc.getPopularityLikeCount()).isEqualTo(1500L);
        }

        @Test
        @DisplayName("Popularity가 null이면 개별 필드도 null")
        void newDoc_withNullPopularity_setsFieldsToNull() {
            // Given
            when(mockVectorizer.vectorize(anyString())).thenReturn(List.of(0.1f, 0.2f, 0.3f, 0.4f, 0.5f));

            Instant now = Instant.now();
            TechBlog techBlog = new TechBlog(
                    1L, "url", null, "title", null, "summary", "서머리ㅣ","body", null,
                    null, null, null, // popularity null
                    false, now, now
            );

            // When
            TechBlogDoc doc = mapper.newDoc(techBlog);

            // Then
            assertThat(doc.getPopularityViewCount()).isNull();
            assertThat(doc.getPopularityCommentCount()).isNull();
            assertThat(doc.getPopularityLikeCount()).isNull();
        }
    }

    @Nested
    @DisplayName("벡터화 테스트")
    class VectorizationTest {

        @Test
        @DisplayName("summary를 기반으로 벡터 생성")
        void newDoc_vectorizesBasedOnSummary() {
            // Given
            Instant now = Instant.now();
            String summary = "This is a test summary for vectorization";
            TechBlog techBlog = new TechBlog(
                    1L, "url", null, "title", null, summary, "서머리ㅣ","body", null,
                    null, null, null, false, now, now
            );

            List<Float> expectedVector = List.of(1.0f, 2.0f, 3.0f, 4.0f, 5.0f);
            when(mockVectorizer.vectorize(summary)).thenReturn(expectedVector);

            // When
            TechBlogDoc doc = mapper.newDoc(techBlog);

            // Then
            assertThat(doc.getVector()).isEqualTo(expectedVector);
        }
    }
}
