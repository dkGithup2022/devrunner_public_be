package dev.devrunner.jdbc.techblog.repository;

import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.common.TechCategory;
import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.model.techblog.TechBlogIdentity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TechBlogJdbcRepository 테스트
 *
 * @DataJdbcTest를 사용한 Spring Data JDBC 통합 테스트
 * Entity ↔ Domain 변환 로직 및 커스텀 쿼리 메서드 검증
 */
@DataJdbcTest
@ComponentScan("dev.devrunner.jdbc.techblog.repository")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TechBlogJdbcRepositoryTest {

    @Autowired
    private TechBlogJdbcRepository techBlogRepository;

    // 테스트 데이터
    private final TechBlog sampleTechBlog = new TechBlog(
            null,                               // techBlogId (자동 생성)
            "https://example.com/blog1",        // url
            "CompanyA",                         // company
            "Test Blog Title",                  // title
            "One liner summary",                // oneLiner
            "Full summary content",             // summary
            "풀 서머리 콘텐트",
            "# Markdown Body",                  // markdownBody
            "https://example.com/thumb.png",    // thumbnailUrl
            List.of(TechCategory.JAVA, TechCategory.SPRING), // techCategories
            "https://original.com/blog1",       // originalUrl
            Popularity.empty(),                 // popularity
            false,                              // isDeleted
            Instant.now(),                      // createdAt
            Instant.now()                       // updatedAt
    );

    private final TechBlogIdentity testIdentity = new TechBlogIdentity(1L);
    private final TechBlogIdentity nonExistingIdentity = new TechBlogIdentity(999L);

    // ========== Entity↔Domain 변환 테스트 ==========

    @Test
    void save_withValidDomain_returnsConvertedDomain() {
        // given
        TechBlog blogToSave = sampleTechBlog;

        // when
        TechBlog saved = techBlogRepository.save(blogToSave);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getTechBlogId()).isNotNull();
        assertThat(saved.getUrl()).isEqualTo(blogToSave.getUrl());
        assertThat(saved.getTitle()).isEqualTo(blogToSave.getTitle());
        assertThat(saved.getCompany()).isEqualTo(blogToSave.getCompany());
        assertThat(saved.getTechCategories()).containsExactlyInAnyOrder(TechCategory.JAVA, TechCategory.SPRING);
    }

    @Test
    void save_withNullId_generatesIdAndReturns() {
        // given
        TechBlog blogWithNullId = sampleTechBlog;

        // when
        TechBlog saved = techBlogRepository.save(blogWithNullId);

        // then
        assertThat(saved.getTechBlogId()).isNotNull();
        assertThat(saved.getUrl()).isEqualTo(blogWithNullId.getUrl());
    }

    @Test
    void findById_existingId_returnsConvertedDomain() {
        // given
        TechBlog saved = techBlogRepository.save(sampleTechBlog);
        TechBlogIdentity identity = new TechBlogIdentity(saved.getTechBlogId());

        // when
        Optional<TechBlog> found = techBlogRepository.findById(identity);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTechBlogId()).isEqualTo(saved.getTechBlogId());
        assertThat(found.get().getUrl()).isEqualTo(saved.getUrl());
        assertThat(found.get().getTitle()).isEqualTo(saved.getTitle());
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        // when
        Optional<TechBlog> found = techBlogRepository.findById(nonExistingIdentity);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_withData_returnsConvertedList() {
        // given
        TechBlog saved1 = techBlogRepository.save(sampleTechBlog);
        TechBlog saved2 = techBlogRepository.save(new TechBlog(
                null, "https://example.com/blog2", "CompanyB", "Another Blog", null, null, null,
                "# Content", null, List.of(), null, Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));

        // when
        List<TechBlog> all = techBlogRepository.findAll();

        // then
        assertThat(all).hasSize(2);
        assertThat(all).extracting(TechBlog::getTechBlogId)
                .containsExactlyInAnyOrder(saved1.getTechBlogId(), saved2.getTechBlogId());
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        // when
        List<TechBlog> all = techBlogRepository.findAll();

        // then
        assertThat(all).isEmpty();
    }

    // ========== Derived Query 테스트 ==========

    @Test
    void findByUrl_existingUrl_returnsConvertedDomain() {
        // given
        TechBlog saved = techBlogRepository.save(sampleTechBlog);

        // when
        Optional<TechBlog> found = techBlogRepository.findByUrl("https://example.com/blog1");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTechBlogId()).isEqualTo(saved.getTechBlogId());
        assertThat(found.get().getUrl()).isEqualTo(saved.getUrl());
    }

    @Test
    void findByUrl_nonExistingUrl_returnsEmpty() {
        // when
        Optional<TechBlog> found = techBlogRepository.findByUrl("https://nonexistent.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByCompany_existingCompany_returnsConvertedList() {
        // given
        TechBlog saved1 = techBlogRepository.save(sampleTechBlog);
        TechBlog saved2 = techBlogRepository.save(new TechBlog(
                null, "https://example.com/blog3", "CompanyA", "Another Blog from A", null, null, null,
                "# Content", null, List.of(), null, Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));
        techBlogRepository.save(new TechBlog(
                null, "https://example.com/blog4", "CompanyB", "Blog from B", null, null, null,
                "# Content", null, List.of(), null, Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));

        // when
        List<TechBlog> found = techBlogRepository.findByCompany("CompanyA");

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(TechBlog::getTechBlogId)
                .containsExactlyInAnyOrder(saved1.getTechBlogId(), saved2.getTechBlogId());
    }

    @Test
    void findByCompany_nonExistingCompany_returnsEmptyList() {
        // when
        List<TechBlog> found = techBlogRepository.findByCompany("NonExistentCompany");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByIdsIn_existingIds_returnsConvertedList() {
        // given
        TechBlog saved1 = techBlogRepository.save(sampleTechBlog);
        TechBlog saved2 = techBlogRepository.save(new TechBlog(
                null, "https://example.com/blog2", "CompanyB", "Another Blog", null, null, null,
                "# Content", null, List.of(), null, Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));
        TechBlog saved3 = techBlogRepository.save(new TechBlog(
                null, "https://example.com/blog3", "CompanyC", "Third Blog", null, null, null,
                "# Content", null, List.of(), null, Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));

        // when
        List<TechBlog> found = techBlogRepository.findByIdsIn(List.of(
                new TechBlogIdentity(saved1.getTechBlogId()),
                new TechBlogIdentity(saved3.getTechBlogId())
        ));

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(TechBlog::getTechBlogId)
                .containsExactlyInAnyOrder(saved1.getTechBlogId(), saved3.getTechBlogId());
    }

    @Test
    void findByIdsIn_nonExistingIds_returnsEmptyList() {
        // when
        List<TechBlog> found = techBlogRepository.findByIdsIn(List.of(
                new TechBlogIdentity(999L),
                new TechBlogIdentity(998L)
        ));

        // then
        assertThat(found).isEmpty();
    }

    // ========== Custom Query 테스트 (@Modifying) ==========

    @Test
    void increaseViewCount_validBlogId_incrementsSuccessfully() {
        // given
        TechBlog saved = techBlogRepository.save(sampleTechBlog);
        TechBlogIdentity identity = new TechBlogIdentity(saved.getTechBlogId());
        long originalViewCount = saved.getPopularity().getViewCount();

        // when
        techBlogRepository.increaseViewCount(identity, 5L);

        // then
        TechBlog updated = techBlogRepository.findById(identity).orElseThrow();
        assertThat(updated.getPopularity().getViewCount()).isEqualTo(originalViewCount + 5L);
    }

    @Test
    void increaseCommentCount_validBlogId_incrementsSuccessfully() {
        // given
        TechBlog saved = techBlogRepository.save(sampleTechBlog);
        TechBlogIdentity identity = new TechBlogIdentity(saved.getTechBlogId());
        long originalCommentCount = saved.getPopularity().getCommentCount();

        // when
        techBlogRepository.increaseCommentCount(identity);

        // then
        TechBlog updated = techBlogRepository.findById(identity).orElseThrow();
        assertThat(updated.getPopularity().getCommentCount()).isEqualTo(originalCommentCount + 1L);
    }

    @Test
    void existsById_existingId_returnsTrue() {
        // given
        TechBlog saved = techBlogRepository.save(sampleTechBlog);
        TechBlogIdentity identity = new TechBlogIdentity(saved.getTechBlogId());

        // when
        boolean exists = techBlogRepository.existsById(identity);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_nonExistingId_returnsFalse() {
        // when
        boolean exists = techBlogRepository.existsById(nonExistingIdentity);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void deleteById_existingId_deletesSuccessfully() {
        // given
        TechBlog saved = techBlogRepository.save(sampleTechBlog);
        TechBlogIdentity identity = new TechBlogIdentity(saved.getTechBlogId());

        // when
        techBlogRepository.deleteById(identity);

        // then
        assertThat(techBlogRepository.existsById(identity)).isFalse();
    }
}
