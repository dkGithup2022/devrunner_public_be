package dev.devrunner.jdbc.communitypost.repository;

import dev.devrunner.jdbc.user.repository.UserJdbcRepository;
import dev.devrunner.model.common.Popularity;
import dev.devrunner.model.communitypost.*;
import dev.devrunner.model.user.User;
import org.junit.jupiter.api.BeforeEach;
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
 * CommunityPostJdbcRepository 테스트
 *
 * @DataJdbcTest를 사용한 Spring Data JDBC 통합 테스트
 * Entity ↔ Domain 변환 로직 및 Custom Query 메서드 검증
 * User JOIN이 필요하므로 UserJdbcRepository와 EmailEncryptor 포함
 */
@DataJdbcTest
@ComponentScan(basePackages = {
        "dev.devrunner.jdbc.communitypost.repository",
        "dev.devrunner.jdbc.user.repository",
        "dev.devrunner.encryption"  // EmailEncryptor 포함
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommunityPostJdbcRepositoryTest {

    @Autowired
    private CommunityPostJdbcRepository communityPostRepository;

    @Autowired
    private UserJdbcRepository userRepository;

    private Long testUser1Id;
    private Long testUser2Id;

    @BeforeEach
    void setUp() {
        // 테스트용 User 저장
        User user1 = User.newUser("google1", "user1@test.com", "TestUser1");
        User savedUser1 = userRepository.save(user1);
        testUser1Id = savedUser1.getUserId();

        User user2 = User.newUser("google2", "user2@test.com", "TestUser2");
        User savedUser2 = userRepository.save(user2);
        testUser2Id = savedUser2.getUserId();
    }

    // 테스트 데이터
    private CommunityPost createSamplePost() {
        return new CommunityPost(
                null,                               // communityPostId (자동 생성)
                testUser1Id,                        // userId
                CommunityPostCategory.INTERVIEW_SHARE,    // category
                "Test Post Title",                  // title
                "# Test Content",                   // markdownBody
                "CompanyA",                         // company
                "Seoul",                            // location
                LinkedContent.none(),               // linkedContent
                Popularity.empty(),                 // popularity
                false,                              // isDeleted
                Instant.now(),                      // createdAt
                Instant.now()                       // updatedAt
        );
    }

    private final CommunityPostIdentity testIdentity = new CommunityPostIdentity(1L);
    private final CommunityPostIdentity nonExistingIdentity = new CommunityPostIdentity(999L);

    // ========== Entity↔Domain 변환 테스트 ==========

    @Test
    void save_withValidDomain_returnsConvertedDomain() {
        // given
        CommunityPost postToSave = createSamplePost();

        // when
        CommunityPost saved = communityPostRepository.save(postToSave);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getCommunityPostId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(postToSave.getUserId());
        assertThat(saved.getTitle()).isEqualTo(postToSave.getTitle());
        assertThat(saved.getCategory()).isEqualTo(postToSave.getCategory());
        assertThat(saved.getCompany()).isEqualTo(postToSave.getCompany());
    }

    @Test
    void save_withNullId_generatesIdAndReturns() {
        // given
        CommunityPost postWithNullId = createSamplePost();

        // when
        CommunityPost saved = communityPostRepository.save(postWithNullId);

        // then
        assertThat(saved.getCommunityPostId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo(postWithNullId.getTitle());
    }

    @Test
    void findById_existingId_returnsConvertedDomain() {
        // given
        CommunityPost saved = communityPostRepository.save(createSamplePost());
        CommunityPostIdentity identity = new CommunityPostIdentity(saved.getCommunityPostId());

        // when
        Optional<CommunityPostRead> found = communityPostRepository.findById(identity);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getCommunityPostId()).isEqualTo(saved.getCommunityPostId());
        assertThat(found.get().getUserId()).isEqualTo(saved.getUserId());
        assertThat(found.get().getNickname()).isEqualTo("TestUser1"); // User JOIN 검증
        assertThat(found.get().getTitle()).isEqualTo(saved.getTitle());
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        // when
        Optional<CommunityPostRead> found = communityPostRepository.findById(nonExistingIdentity);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_withData_returnsConvertedList() {
        // given
        CommunityPost saved1 = communityPostRepository.save(createSamplePost());
        CommunityPost saved2 = communityPostRepository.save(new CommunityPost(
                null, testUser2Id, CommunityPostCategory.QUESTION, "Another Post", "# Content",
                null, null, LinkedContent.none(), Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));

        // when
        List<CommunityPostRead> all = communityPostRepository.findAll();

        // then
        assertThat(all).hasSize(2);
        assertThat(all).extracting(CommunityPostRead::getCommunityPostId)
                .containsExactlyInAnyOrder(saved1.getCommunityPostId(), saved2.getCommunityPostId());
        assertThat(all).extracting(CommunityPostRead::getNickname)
                .containsExactlyInAnyOrder("TestUser1", "TestUser2"); // User JOIN 검증
    }

    // ========== Custom Query 테스트 (User JOIN) ==========

    @Test
    void findByUserId_existingUserId_returnsConvertedList() {
        // given
        CommunityPost saved1 = communityPostRepository.save(createSamplePost());
        CommunityPost saved2 = communityPostRepository.save(new CommunityPost(
                null, testUser1Id, CommunityPostCategory.QUESTION, "Another Post", "# Content",
                null, null, LinkedContent.none(), Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));
        communityPostRepository.save(new CommunityPost(
                null, testUser2Id, CommunityPostCategory.CHIT_CHAT, "User2 Post", "# Content",
                null, null, LinkedContent.none(), Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));

        // when
        List<CommunityPostRead> found = communityPostRepository.findByUserId(testUser1Id);

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(CommunityPostRead::getCommunityPostId)
                .containsExactlyInAnyOrder(saved1.getCommunityPostId(), saved2.getCommunityPostId());
        assertThat(found).allMatch(post -> post.getNickname().equals("TestUser1"));
    }

    @Test
    void findByUserId_nonExistingUserId_returnsEmptyList() {
        // when
        List<CommunityPostRead> found = communityPostRepository.findByUserId(999L);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByCompany_existingCompany_returnsConvertedList() {
        // given
        CommunityPost saved1 = communityPostRepository.save(createSamplePost());
        CommunityPost saved2 = communityPostRepository.save(new CommunityPost(
                null, testUser2Id, CommunityPostCategory.TECH_ARTICLE, "Company A Review", "# Content",
                "CompanyA", null, LinkedContent.none(), Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));
        communityPostRepository.save(new CommunityPost(
                null, testUser1Id, CommunityPostCategory.CHIT_CHAT, "Company B Post", "# Content",
                "CompanyB", null, LinkedContent.none(), Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));

        // when
        List<CommunityPostRead> found = communityPostRepository.findByCompany("CompanyA");

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(CommunityPostRead::getCommunityPostId)
                .containsExactlyInAnyOrder(saved1.getCommunityPostId(), saved2.getCommunityPostId());
    }

    @Test
    void findByCompany_nonExistingCompany_returnsEmptyList() {
        // when
        List<CommunityPostRead> found = communityPostRepository.findByCompany("NonExistentCompany");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByLocation_existingLocation_returnsConvertedList() {
        // given
        CommunityPost saved1 = communityPostRepository.save(createSamplePost());
        CommunityPost saved2 = communityPostRepository.save(new CommunityPost(
                null, testUser2Id, CommunityPostCategory.CHIT_CHAT, "Seoul Post", "# Content",
                null, "Seoul", LinkedContent.none(), Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));
        communityPostRepository.save(new CommunityPost(
                null, testUser1Id, CommunityPostCategory.TECH_ARTICLE, "Busan Post", "# Content",
                null, "Busan", LinkedContent.none(), Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));

        // when
        List<CommunityPostRead> found = communityPostRepository.findByLocation("Seoul");

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(CommunityPostRead::getCommunityPostId)
                .containsExactlyInAnyOrder(saved1.getCommunityPostId(), saved2.getCommunityPostId());
    }

    @Test
    void findByLocation_nonExistingLocation_returnsEmptyList() {
        // when
        List<CommunityPostRead> found = communityPostRepository.findByLocation("NonExistentLocation");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByIds_existingIds_returnsConvertedList() {
        // given
        CommunityPost saved1 = communityPostRepository.save(createSamplePost());
        CommunityPost saved2 = communityPostRepository.save(new CommunityPost(
                null, testUser2Id, CommunityPostCategory.QUESTION, "Another Post", "# Content",
                null, null, LinkedContent.none(), Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));
        CommunityPost saved3 = communityPostRepository.save(new CommunityPost(
                null, testUser1Id, CommunityPostCategory.INTERVIEW_SHARE, "Third Post", "# Content",
                null, null, LinkedContent.none(), Popularity.empty(), false,
                Instant.now(), Instant.now()
        ));

        // when
        List<CommunityPostRead> found = communityPostRepository.findByIds(List.of(
                new CommunityPostIdentity(saved1.getCommunityPostId()),
                new CommunityPostIdentity(saved3.getCommunityPostId())
        ));

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(CommunityPostRead::getCommunityPostId)
                .containsExactlyInAnyOrder(saved1.getCommunityPostId(), saved3.getCommunityPostId());
    }

    @Test
    void findByIds_nonExistingIds_returnsEmptyList() {
        // when
        List<CommunityPostRead> found = communityPostRepository.findByIds(List.of(
                new CommunityPostIdentity(999L),
                new CommunityPostIdentity(998L)
        ));

        // then
        assertThat(found).isEmpty();
    }

    // ========== @Modifying Custom Query 테스트 ==========

    @Test
    void increaseViewCount_validPostId_incrementsSuccessfully() {
        // given
        CommunityPost saved = communityPostRepository.save(createSamplePost());
        CommunityPostIdentity identity = new CommunityPostIdentity(saved.getCommunityPostId());
        long originalViewCount = saved.getPopularity().getViewCount();

        // when
        communityPostRepository.increaseViewCount(identity, 5L);

        // then
        Optional<CommunityPostRead> updated = communityPostRepository.findById(identity);
        assertThat(updated).isPresent();
        assertThat(updated.get().getPopularity().getViewCount()).isEqualTo(originalViewCount + 5L);
    }

    @Test
    void increaseCommentCount_validPostId_incrementsSuccessfully() {
        // given
        CommunityPost saved = communityPostRepository.save(createSamplePost());
        CommunityPostIdentity identity = new CommunityPostIdentity(saved.getCommunityPostId());
        long originalCommentCount = saved.getPopularity().getCommentCount();

        // when
        communityPostRepository.increaseCommentCount(identity);

        // then
        Optional<CommunityPostRead> updated = communityPostRepository.findById(identity);
        assertThat(updated).isPresent();
        assertThat(updated.get().getPopularity().getCommentCount()).isEqualTo(originalCommentCount + 1L);
    }

    @Test
    void increaseLikeCount_validPostId_incrementsSuccessfully() {
        // given
        CommunityPost saved = communityPostRepository.save(createSamplePost());
        CommunityPostIdentity identity = new CommunityPostIdentity(saved.getCommunityPostId());
        long originalLikeCount = saved.getPopularity().getLikeCount();

        // when
        communityPostRepository.increaseLikeCount(identity, 3L);

        // then
        Optional<CommunityPostRead> updated = communityPostRepository.findById(identity);
        assertThat(updated).isPresent();
        assertThat(updated.get().getPopularity().getLikeCount()).isEqualTo(originalLikeCount + 3L);
    }

    @Test
    void increaseDislikeCount_validPostId_incrementsSuccessfully() {
        // given
        CommunityPost saved = communityPostRepository.save(createSamplePost());
        CommunityPostIdentity identity = new CommunityPostIdentity(saved.getCommunityPostId());
        long originalDislikeCount = saved.getPopularity().getDislikeCount();

        // when
        communityPostRepository.increaseDislikeCount(identity, 2L);

        // then
        Optional<CommunityPostRead> updated = communityPostRepository.findById(identity);
        assertThat(updated).isPresent();
        assertThat(updated.get().getPopularity().getDislikeCount()).isEqualTo(originalDislikeCount + 2L);
    }

    @Test
    void existsById_existingId_returnsTrue() {
        // given
        CommunityPost saved = communityPostRepository.save(createSamplePost());
        CommunityPostIdentity identity = new CommunityPostIdentity(saved.getCommunityPostId());

        // when
        boolean exists = communityPostRepository.existsById(identity);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_nonExistingId_returnsFalse() {
        // when
        boolean exists = communityPostRepository.existsById(nonExistingIdentity);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void deleteById_existingId_deletesSuccessfully() {
        // given
        CommunityPost saved = communityPostRepository.save(createSamplePost());
        CommunityPostIdentity identity = new CommunityPostIdentity(saved.getCommunityPostId());

        // when
        communityPostRepository.deleteById(identity);

        // then
        assertThat(communityPostRepository.existsById(identity)).isFalse();
    }
}
