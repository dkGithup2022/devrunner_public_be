package dev.devrunner.service.comment.impl.integration;

import dev.devrunner.infra.comment.repository.CommentRepository;
import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.infra.user.repository.UserRepository;
import dev.devrunner.model.comment.Comment;
import dev.devrunner.model.comment.CommentIdentity;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.job.Job;
import dev.devrunner.model.user.User;
import dev.devrunner.service.comment.CommentWriter;
import dev.devrunner.service.comment.dto.CommentWriteCommand;
import dev.devrunner.service.integrationConfig.IntegrationTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CommentWriter 동시성 테스트
 * <p>
 * 동시에 여러 댓글이 작성될 때 commentOrder와 sortNumber가 올바르게 할당되는지 검증
 */
@Slf4j
class DefaultCommentWriterConcurrencyTest extends IntegrationTestBase {

    @Autowired
    private CommentWriter commentWriter;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;
    private Job testJob;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성
        testUser = User.newUser("google-123", "test@example.com", "testUser");
        testUser = userRepository.save(testUser);

        // 테스트용 Job 생성
        testJob = Job.newJob(
                "https://example.com/job/1",
                Company.NAVER,
                "Backend Engineer",
                "Engineering Team",
                "desc",
                List.of()
        );
        testJob = jobRepository.save(testJob);
    }

    @AfterEach
    void clearAll() {
        // 테스트 후 데이터 정리 (역순으로 삭제 - FK 관계 고려)
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM jobs");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    @DisplayName("10개의 최상위 댓글을 동시에 생성하면 commentOrder가 1~10으로 중복 없이 할당됨")
    void writeRootComments_concurrently_noCommentOrderDuplicates() throws Exception {
        // given
        int threadCount = 10;
        Set<Integer> commentOrders = ConcurrentHashMap.newKeySet();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // when - 10개 스레드가 동시에 최상위 댓글 작성
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    log.info("Ready for comment order {}", index);
                    startLatch.await();  // 모두 대기
                    log.info("Running comment order {}", index);

                    CommentWriteCommand command = CommentWriteCommand.root(
                            testUser.getUserId(),
                            "Root comment " + index,
                            TargetType.JOB,
                            testJob.getJobId()
                    );

                    Comment created = commentWriter.write(command);
                    commentOrders.add(created.getCommentOrder().getCommentOrder());

                    log.info("Comment order done {}", index);
                    doneLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        log.info("start");
        // 동시 시작!
        startLatch.countDown();

        // 모든 스레드 완료 대기
        doneLatch.await();
        executor.shutdown();

        // then - commentOrder가 1~10으로 중복 없이 생성됨
        assertThat(commentOrders).hasSize(threadCount);
        assertThat(commentOrders).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // DB에서 재확인
        var comments = commentRepository.findByTargetTypeAndTargetId(TargetType.JOB, testJob.getJobId());
        assertThat(comments).hasSize(threadCount);

        // 모든 댓글이 level 0 (최상위)
        assertThat(comments)
                .allMatch(c -> c.getCommentOrder().getLevel() == 0)
                .allMatch(c -> c.getCommentOrder().getSortNumber() == 0);
    }

    @Test
    @DisplayName("한 댓글에 대한 10개의 대댓글을 동시에 생성하면 같은 commentOrder에 sortNumber가 1~10으로 순차 증가")
    void writeReplyComments_concurrently_sortNumberSequential() throws Exception {
        // given - 최상위 댓글 1개 생성
        CommentWriteCommand rootCommand = CommentWriteCommand.root(
                testUser.getUserId(),
                "Root comment",
                TargetType.JOB,
                testJob.getJobId()
        );
        Comment rootComment = commentWriter.write(rootCommand);
        Long rootCommentId = rootComment.getCommentId();
        Integer rootCommentOrder = rootComment.getCommentOrder().getCommentOrder();

        int threadCount = 10;
        Set<Integer> sortNumbers = ConcurrentHashMap.newKeySet();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // when - 10개 스레드가 동시에 대댓글 작성
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();  // 모두 대기

                    CommentWriteCommand command = CommentWriteCommand.reply(
                            testUser.getUserId(),
                            "Reply comment " + index,
                            TargetType.JOB,
                            testJob.getJobId(),
                            rootCommentId  // 같은 부모
                    );

                    Comment created = commentWriter.write(command);
                    sortNumbers.add(created.getCommentOrder().getSortNumber());

                    doneLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 동시 시작!
        startLatch.countDown();

        // 모든 스레드 완료 대기
        doneLatch.await();
        executor.shutdown();

        // then - sortNumber가 1~10으로 중복 없이 생성됨
        assertThat(sortNumbers).hasSize(threadCount);
        assertThat(sortNumbers).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // DB에서 재확인
        var allComments = commentRepository.findByTargetTypeAndTargetId(TargetType.JOB, testJob.getJobId());
        var replies = allComments.stream()
                .filter(c -> c.getCommentOrder().getLevel() == 1)  // 대댓글만
                .toList();

        assertThat(replies).hasSize(threadCount);

        // 모든 대댓글이 같은 commentOrder
        assertThat(replies)
                .allMatch(c -> c.getCommentOrder().getCommentOrder().equals(rootCommentOrder));

        // 모든 대댓글이 level 1
        assertThat(replies)
                .allMatch(c -> c.getCommentOrder().getLevel() == 1);

        // sortNumber가 1~10 (중복 없음)
        Set<Integer> actualSortNumbers = replies.stream()
                .map(c -> c.getCommentOrder().getSortNumber())
                .collect(java.util.stream.Collectors.toSet());
        assertThat(actualSortNumbers).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // 부모 댓글의 childCount가 10으로 증가했는지 확인
        var updatedRoot = commentRepository.findById(new CommentIdentity(rootCommentId))
                .orElseThrow();
        assertThat(updatedRoot.getCommentOrder().getChildCount()).isEqualTo(10);
    }
}
