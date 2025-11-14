package dev.devrunner.jdbc.bookmark.repository;

import dev.devrunner.model.bookmark.Bookmark;
import dev.devrunner.model.bookmark.BookmarkIdentity;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.user.UserIdentity;
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
 * BookmarkJdbcRepository 테스트
 *
 * @DataJdbcTest를 사용한 Spring Data JDBC 통합 테스트
 * Entity ↔ Domain 변환 로직 및 Derived Query 메서드 검증
 */
@DataJdbcTest
@ComponentScan("dev.devrunner.jdbc.bookmark.repository")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookmarkJdbcRepositoryTest {

    @Autowired
    private BookmarkJdbcRepository bookmarkRepository;

    // 테스트 데이터
    private final Bookmark sampleBookmark = new Bookmark(
            null,                   // bookmarkId (자동 생성)
            1L,                     // userId
            TargetType.JOB,         // targetType
            100L,                   // targetId
            Instant.now(),          // createdAt
            Instant.now()           // updatedAt
    );

    private final BookmarkIdentity testIdentity = new BookmarkIdentity(1L);
    private final BookmarkIdentity nonExistingIdentity = new BookmarkIdentity(999L);

    // ========== Entity↔Domain 변환 테스트 ==========

    @Test
    void save_withValidDomain_returnsConvertedDomain() {
        // given
        Bookmark bookmarkToSave = sampleBookmark;

        // when
        Bookmark saved = bookmarkRepository.save(bookmarkToSave);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getBookmarkId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(bookmarkToSave.getUserId());
        assertThat(saved.getTargetType()).isEqualTo(bookmarkToSave.getTargetType());
        assertThat(saved.getTargetId()).isEqualTo(bookmarkToSave.getTargetId());
    }

    @Test
    void save_withNullId_generatesIdAndReturns() {
        // given
        Bookmark bookmarkWithNullId = sampleBookmark;

        // when
        Bookmark saved = bookmarkRepository.save(bookmarkWithNullId);

        // then
        assertThat(saved.getBookmarkId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(bookmarkWithNullId.getUserId());
    }

    @Test
    void findById_existingId_returnsConvertedDomain() {
        // given
        Bookmark saved = bookmarkRepository.save(sampleBookmark);
        BookmarkIdentity identity = new BookmarkIdentity(saved.getBookmarkId());

        // when
        Optional<Bookmark> found = bookmarkRepository.findById(identity);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getBookmarkId()).isEqualTo(saved.getBookmarkId());
        assertThat(found.get().getUserId()).isEqualTo(saved.getUserId());
        assertThat(found.get().getTargetType()).isEqualTo(saved.getTargetType());
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        // when
        Optional<Bookmark> found = bookmarkRepository.findById(nonExistingIdentity);

        // then
        assertThat(found).isEmpty();
    }

    // ========== Derived Query 테스트 ==========

    @Test
    void findByUserId_existingUserId_returnsConvertedList() {
        // given
        Bookmark saved1 = bookmarkRepository.save(sampleBookmark);
        Bookmark saved2 = bookmarkRepository.save(new Bookmark(
                null, 1L, TargetType.TECH_BLOG, 200L,
                Instant.now(), Instant.now()
        ));
        bookmarkRepository.save(new Bookmark(
                null, 2L, TargetType.JOB, 100L,
                Instant.now(), Instant.now()
        ));

        // when
        List<Bookmark> found = bookmarkRepository.findByUserId(new UserIdentity(1L), 0, 10);

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Bookmark::getBookmarkId)
                .containsExactlyInAnyOrder(saved1.getBookmarkId(), saved2.getBookmarkId());
    }

    @Test
    void findByUserId_nonExistingUserId_returnsEmptyList() {
        // when
        List<Bookmark> found = bookmarkRepository.findByUserId(new UserIdentity(999L), 0, 10);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByUserIdAndTargetType_existingData_returnsConvertedList() {
        // given
        Bookmark saved1 = bookmarkRepository.save(sampleBookmark);
        Bookmark saved2 = bookmarkRepository.save(new Bookmark(
                null, 1L, TargetType.JOB, 200L,
                Instant.now(), Instant.now()
        ));
        bookmarkRepository.save(new Bookmark(
                null, 1L, TargetType.TECH_BLOG, 300L,
                Instant.now(), Instant.now()
        ));

        // when
        List<Bookmark> found = bookmarkRepository.findByUserIdAndTargetType(
                new UserIdentity(1L), TargetType.JOB, 0, 10
        );

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Bookmark::getBookmarkId)
                .containsExactlyInAnyOrder(saved1.getBookmarkId(), saved2.getBookmarkId());
    }

    @Test
    void findByUserIdAndTargetType_nonExistingData_returnsEmptyList() {
        // when
        List<Bookmark> found = bookmarkRepository.findByUserIdAndTargetType(
                new UserIdentity(999L), TargetType.JOB, 0, 10
        );

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void countByUserId_existingUserId_returnsCount() {
        // given
        bookmarkRepository.save(sampleBookmark);
        bookmarkRepository.save(new Bookmark(
                null, 1L, TargetType.TECH_BLOG, 200L,
                Instant.now(), Instant.now()
        ));
        bookmarkRepository.save(new Bookmark(
                null, 2L, TargetType.JOB, 100L,
                Instant.now(), Instant.now()
        ));

        // when
        long count = bookmarkRepository.countByUserId(new UserIdentity(1L));

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByUserId_nonExistingUserId_returnsZero() {
        // when
        long count = bookmarkRepository.countByUserId(new UserIdentity(999L));

        // then
        assertThat(count).isZero();
    }

    @Test
    void findByUserIdAndTargetTypeAndTargetId_existingData_returnsConvertedDomain() {
        // given
        Bookmark saved = bookmarkRepository.save(sampleBookmark);

        // when
        Optional<Bookmark> found = bookmarkRepository.findByUserIdAndTargetTypeAndTargetId(
                new UserIdentity(1L), TargetType.JOB, 100L
        );

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getBookmarkId()).isEqualTo(saved.getBookmarkId());
        assertThat(found.get().getUserId()).isEqualTo(saved.getUserId());
    }

    @Test
    void findByUserIdAndTargetTypeAndTargetId_nonExistingData_returnsEmpty() {
        // when
        Optional<Bookmark> found = bookmarkRepository.findByUserIdAndTargetTypeAndTargetId(
                new UserIdentity(999L), TargetType.JOB, 100L
        );

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void existsById_existingId_returnsTrue() {
        // given
        Bookmark saved = bookmarkRepository.save(sampleBookmark);
        BookmarkIdentity identity = new BookmarkIdentity(saved.getBookmarkId());

        // when
        boolean exists = bookmarkRepository.existsById(identity);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_nonExistingId_returnsFalse() {
        // when
        boolean exists = bookmarkRepository.existsById(nonExistingIdentity);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void deleteById_existingId_deletesSuccessfully() {
        // given
        Bookmark saved = bookmarkRepository.save(sampleBookmark);
        BookmarkIdentity identity = new BookmarkIdentity(saved.getBookmarkId());

        // when
        bookmarkRepository.deleteById(identity);

        // then
        assertThat(bookmarkRepository.existsById(identity)).isFalse();
    }
}
