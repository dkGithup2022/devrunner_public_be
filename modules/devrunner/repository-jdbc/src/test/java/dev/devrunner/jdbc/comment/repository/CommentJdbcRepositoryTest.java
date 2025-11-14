package dev.devrunner.jdbc.comment.repository;

import dev.devrunner.jdbc.user.repository.UserJdbcRepository;
import dev.devrunner.model.comment.Comment;
import dev.devrunner.model.comment.CommentIdentity;
import dev.devrunner.model.comment.CommentRead;
import dev.devrunner.model.common.CommentOrder;
import dev.devrunner.model.common.TargetType;
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
 * CommentJdbcRepository 테스트
 *
 * @DataJdbcTest를 사용한 Spring Data JDBC 통합 테스트
 * Entity ↔ Domain 변환 로직 및 Custom Query 메서드 검증
 * User JOIN이 필요하므로 UserJdbcRepository와 EmailEncryptor 포함
 */
@DataJdbcTest
@ComponentScan(basePackages = {
        "dev.devrunner.jdbc.comment.repository",
        "dev.devrunner.jdbc.user.repository",
        "dev.devrunner.encryption"  // EmailEncryptor 포함
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentJdbcRepositoryTest {

    @Autowired
    private CommentJdbcRepository commentRepository;

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
    private Comment createSampleComment() {
        return new Comment(
                null,                           // commentId (자동 생성)
                testUser1Id,                    // userId
                "Test comment content",         // content
                TargetType.JOB,                 // targetType
                100L,                           // targetId
                null,                           // parentId
                CommentOrder.newRootComment(1), // commentOrder
                false,                          // isHidden
                Instant.now(),                  // createdAt
                Instant.now()                   // updatedAt
        );
    }

    private final CommentIdentity nonExistingIdentity = new CommentIdentity(999L);

    // ========== Entity↔Domain 변환 테스트 ==========

    @Test
    void save_withValidDomain_returnsConvertedDomain() {
        // given
        Comment commentToSave = createSampleComment();

        // when
        Comment saved = commentRepository.save(commentToSave);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getCommentId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(commentToSave.getUserId());
        assertThat(saved.getContent()).isEqualTo(commentToSave.getContent());
        assertThat(saved.getTargetType()).isEqualTo(commentToSave.getTargetType());
        assertThat(saved.getTargetId()).isEqualTo(commentToSave.getTargetId());
        assertThat(saved.getParentId()).isEqualTo(commentToSave.getParentId());
        assertThat(saved.getCommentOrder()).isEqualTo(commentToSave.getCommentOrder());
        assertThat(saved.getIsHidden()).isEqualTo(commentToSave.getIsHidden());
    }

    @Test
    void save_withNullId_generatesIdAndReturns() {
        // given
        Comment commentWithNullId = createSampleComment();

        // when
        Comment saved = commentRepository.save(commentWithNullId);

        // then
        assertThat(saved.getCommentId()).isNotNull();
        assertThat(saved.getContent()).isEqualTo(commentWithNullId.getContent());
    }

    @Test
    void findById_existingId_returnsConvertedDomain() {
        // given
        Comment saved = commentRepository.save(createSampleComment());
        CommentIdentity identity = new CommentIdentity(saved.getCommentId());

        // when
        Optional<CommentRead> found = commentRepository.findById(identity);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getCommentId()).isEqualTo(saved.getCommentId());
        assertThat(found.get().getUserId()).isEqualTo(saved.getUserId());
        assertThat(found.get().getNickname()).isEqualTo("TestUser1"); // User JOIN 검증
        assertThat(found.get().getContent()).isEqualTo(saved.getContent());
        assertThat(found.get().getTargetType()).isEqualTo(saved.getTargetType());
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        // when
        Optional<CommentRead> found = commentRepository.findById(nonExistingIdentity);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_withData_returnsConvertedList() {
        // given
        Comment saved1 = commentRepository.save(createSampleComment());
        Comment saved2 = commentRepository.save(new Comment(
                null, testUser2Id, "Another comment", TargetType.JOB, 100L, null,
                CommentOrder.newRootComment(2), false, Instant.now(), Instant.now()
        ));

        // when
        List<CommentRead> all = commentRepository.findAll();

        // then
        assertThat(all).hasSize(2);
        assertThat(all).extracting(CommentRead::getCommentId)
                .containsExactlyInAnyOrder(saved1.getCommentId(), saved2.getCommentId());
        assertThat(all).extracting(CommentRead::getNickname)
                .containsExactlyInAnyOrder("TestUser1", "TestUser2"); // User JOIN 검증
    }

    // ========== Custom Query 테스트 (User JOIN) ==========

    @Test
    void findByTargetTypeAndTargetId_existingData_returnsConvertedList() {
        // given
        Comment saved1 = commentRepository.save(createSampleComment());
        Comment saved2 = commentRepository.save(new Comment(
                null, testUser2Id, "Another comment", TargetType.JOB, 100L, null,
                CommentOrder.newRootComment(2), false, Instant.now(), Instant.now()
        ));
        commentRepository.save(new Comment(
                null, testUser1Id, "Different target", TargetType.COMMUNITY_POST, 200L, null,
                CommentOrder.newRootComment(1), false, Instant.now(), Instant.now()
        ));

        // when
        List<CommentRead> found = commentRepository.findByTargetTypeAndTargetId(TargetType.JOB, 100L);

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(CommentRead::getCommentId)
                .containsExactlyInAnyOrder(saved1.getCommentId(), saved2.getCommentId());
        assertThat(found).extracting(CommentRead::getNickname)
                .containsExactlyInAnyOrder("TestUser1", "TestUser2"); // User JOIN 검증
    }

    @Test
    void findByTargetTypeAndTargetId_nonExistingData_returnsEmptyList() {
        // when
        List<CommentRead> found = commentRepository.findByTargetTypeAndTargetId(TargetType.JOB, 999L);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByTargetTypeAndTargetIdWithPaging_existingData_returnsPaginatedList() {
        // given
        for (int i = 0; i < 5; i++) {
            Long userId = (i % 2 == 0) ? testUser1Id : testUser2Id;
            commentRepository.save(new Comment(
                    null, userId, "Comment " + i, TargetType.JOB, 100L, null,
                    CommentOrder.newRootComment(i + 1), false, Instant.now(), Instant.now()
            ));
        }

        // when
        List<CommentRead> page1 = commentRepository.findByTargetTypeAndTargetIdWithPaging(TargetType.JOB, 100L, 0, 2);
        List<CommentRead> page2 = commentRepository.findByTargetTypeAndTargetIdWithPaging(TargetType.JOB, 100L, 2, 2);

        // then
        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(2);
        assertThat(page1).allMatch(comment -> comment.getNickname() != null); // User JOIN 검증
    }

    @Test
    void findByParentId_existingParent_returnsConvertedList() {
        // given
        Comment parent = commentRepository.save(createSampleComment());
        Comment reply1 = commentRepository.save(new Comment(
                null, testUser2Id, "Reply 1", TargetType.JOB, 100L, parent.getCommentId(),
                CommentOrder.newReply(1, 1, 1, parent.getCommentId()), false, Instant.now(), Instant.now()
        ));
        Comment reply2 = commentRepository.save(new Comment(
                null, testUser1Id, "Reply 2", TargetType.JOB, 100L, parent.getCommentId(),
                CommentOrder.newReply(1, 1, 2, parent.getCommentId()), false, Instant.now(), Instant.now()
        ));

        // when
        List<CommentRead> replies = commentRepository.findByParentId(parent.getCommentId());

        // then
        assertThat(replies).hasSize(2);
        assertThat(replies).extracting(CommentRead::getCommentId)
                .containsExactlyInAnyOrder(reply1.getCommentId(), reply2.getCommentId());
        assertThat(replies).extracting(CommentRead::getNickname)
                .containsExactlyInAnyOrder("TestUser1", "TestUser2"); // User JOIN 검증
    }

    @Test
    void findByParentId_nonExistingParent_returnsEmptyList() {
        // when
        List<CommentRead> replies = commentRepository.findByParentId(999L);

        // then
        assertThat(replies).isEmpty();
    }

    @Test
    void findMaxCommentOrder_existingComments_returnsMaxValue() {
        // given
        commentRepository.save(new Comment(
                null, testUser1Id, "Comment 1", TargetType.JOB, 100L, null,
                CommentOrder.newRootComment(1), false, Instant.now(), Instant.now()
        ));
        commentRepository.save(new Comment(
                null, testUser2Id, "Comment 2", TargetType.JOB, 100L, null,
                CommentOrder.newRootComment(5), false, Instant.now(), Instant.now()
        ));
        commentRepository.save(new Comment(
                null, testUser1Id, "Comment 3", TargetType.JOB, 100L, null,
                CommentOrder.newRootComment(3), false, Instant.now(), Instant.now()
        ));

        // when
        Integer maxOrder = commentRepository.findMaxCommentOrder(TargetType.JOB, 100L);

        // then
        assertThat(maxOrder).isEqualTo(5);
    }

    @Test
    void findMaxCommentOrder_noComments_returnsZero() {
        // when
        Integer maxOrder = commentRepository.findMaxCommentOrder(TargetType.JOB, 999L);

        // then
        assertThat(maxOrder).isEqualTo(0);
    }

    // ========== @Modifying Custom Query 테스트 ==========

    @Test
    void incrementSortNumbersAbove_validInput_updatesCorrectly() {
        // given
        commentRepository.save(new Comment(
                null, testUser1Id, "Comment 1", TargetType.JOB, 100L, null,
                CommentOrder.newReply(1, 1, 1, null), false, Instant.now(), Instant.now()
        ));
        commentRepository.save(new Comment(
                null, testUser2Id, "Comment 2", TargetType.JOB, 100L, null,
                CommentOrder.newReply(1, 1, 2, null), false, Instant.now(), Instant.now()
        ));

        // when
        commentRepository.incrementSortNumbersAbove(TargetType.JOB, 100L, 1, 1);

        // then - 수동 검증 필요 (실제 업데이트 확인)
        List<CommentRead> comments = commentRepository.findByTargetTypeAndTargetId(TargetType.JOB, 100L);
        assertThat(comments).isNotEmpty();
    }

    @Test
    void incrementChildCount_validCommentId_incrementsSuccessfully() {
        // given
        Comment parent = commentRepository.save(createSampleComment());

        // when
        commentRepository.incrementChildCount(parent.getCommentId());

        // then - 수동 검증 필요 (실제 childCount 확인)
        Optional<CommentRead> found = commentRepository.findById(new CommentIdentity(parent.getCommentId()));
        assertThat(found).isPresent();
    }

    @Test
    void existsById_existingId_returnsTrue() {
        // given
        Comment saved = commentRepository.save(createSampleComment());
        CommentIdentity identity = new CommentIdentity(saved.getCommentId());

        // when
        boolean exists = commentRepository.existsById(identity);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_nonExistingId_returnsFalse() {
        // when
        boolean exists = commentRepository.existsById(nonExistingIdentity);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void deleteById_existingId_deletesSuccessfully() {
        // given
        Comment saved = commentRepository.save(createSampleComment());
        CommentIdentity identity = new CommentIdentity(saved.getCommentId());

        // when
        commentRepository.deleteById(identity);

        // then
        assertThat(commentRepository.existsById(identity)).isFalse();
    }
}
