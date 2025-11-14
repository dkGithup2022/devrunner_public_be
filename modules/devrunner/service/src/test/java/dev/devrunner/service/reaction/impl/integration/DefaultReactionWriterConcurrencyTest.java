package dev.devrunner.service.reaction.impl.integration;

import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.infra.reaction.repository.ReactionRepository;
import dev.devrunner.infra.user.repository.UserRepository;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.job.Job;
import dev.devrunner.model.job.JobIdentity;
import dev.devrunner.model.reaction.Reaction;
import dev.devrunner.model.user.User;
import dev.devrunner.model.user.UserIdentity;
import dev.devrunner.service.integrationConfig.IntegrationTestBase;
import dev.devrunner.service.reaction.ReactionWriter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReactionWriter 동시성 테스트
 * <p>
 * 동시에 여러 사용자가 좋아요/싫어요를 누를 때 카운터가 정확하게 증가하는지 검증
 */
@Slf4j
class DefaultReactionWriterConcurrencyTest extends IntegrationTestBase {

    @Autowired
    private ReactionWriter reactionWriter;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<User> testUsers;
    private Job testJob;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 10명 생성
        testUsers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = User.newUser("google-" + i, "test" + i + "@example.com", "testUser" + i);
            testUsers.add(userRepository.save(user));
        }

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
        jdbcTemplate.execute("DELETE FROM reactions");
        jdbcTemplate.execute("DELETE FROM jobs");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    @DisplayName("10명의 사용자가 같은 Job에 동시에 좋아요를 누르면 like_count가 정확히 10 증가")
    void likeUp_concurrently_likeCountAccurate() throws Exception {
        // given
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 10명이 동시에 좋아요
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();  // 모두 대기

                    User user = testUsers.get(index);
                    reactionWriter.likeUp(
                            new UserIdentity(user.getUserId()),
                            TargetType.JOB,
                            testJob.getJobId()
                    );

                    successCount.incrementAndGet();
                    log.info("User {} liked successfully", index);
                    doneLatch.countDown();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("User {} failed to like", index, e);
                    doneLatch.countDown();
                }
            });
        }

        // 동시 시작!
        startLatch.countDown();

        // 모든 스레드 완료 대기
        doneLatch.await();
        executor.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failCount.get()).isEqualTo(0);

        // Job의 like_count 확인
        Job updatedJob = jobRepository.findById(new JobIdentity(testJob.getJobId()))
                .orElseThrow();
        assertThat(updatedJob.getPopularity().getLikeCount()).isEqualTo(10L);

        // Reaction이 10개 생성되었는지 확인
        List<Reaction> reactions = reactionRepository.findByTargetTypeAndTargetId(
                TargetType.JOB,
                testJob.getJobId()
        );
        assertThat(reactions).hasSize(10);

        // 각 User의 likeGivenCount가 1씩 증가했는지 확인
        for (User testUser : testUsers) {
            User updatedUser = userRepository.findById(new UserIdentity(testUser.getUserId()))
                    .orElseThrow();
            assertThat(updatedUser.getLikeGivenCount()).isEqualTo(1L);
        }
    }

    @Test
    @DisplayName("10명의 사용자가 같은 Job에 동시에 싫어요를 누르면 dislike_count가 정확히 10 증가")
    void dislikeUp_concurrently_dislikeCountAccurate() throws Exception {
        // given
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when - 10명이 동시에 싫어요
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();  // 모두 대기

                    User user = testUsers.get(index);
                    reactionWriter.dislikeUp(
                            new UserIdentity(user.getUserId()),
                            TargetType.JOB,
                            testJob.getJobId()
                    );

                    successCount.incrementAndGet();
                    doneLatch.countDown();
                } catch (Exception e) {
                    log.error("User {} failed to dislike", index, e);
                    doneLatch.countDown();
                }
            });
        }

        // 동시 시작!
        startLatch.countDown();

        // 모든 스레드 완료 대기
        doneLatch.await();
        executor.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(threadCount);

        // Job의 dislike_count 확인
        Job updatedJob = jobRepository.findById(new JobIdentity(testJob.getJobId()))
                .orElseThrow();
        assertThat(updatedJob.getPopularity().getDislikeCount()).isEqualTo(10L);

        // Reaction이 10개 생성되었는지 확인
        List<Reaction> reactions = reactionRepository.findByTargetTypeAndTargetId(
                TargetType.JOB,
                testJob.getJobId()
        );
        assertThat(reactions).hasSize(10);
    }

    @Test
    @DisplayName("같은 사용자가 동시에 여러 번 좋아요를 시도하면 1번만 성공하고 나머지는 실패")
    void likeUp_sameUserConcurrently_onlyOneSuccess() throws Exception {
        // given
        User singleUser = testUsers.get(0);
        int attemptCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(attemptCount);
        ExecutorService executor = Executors.newFixedThreadPool(attemptCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 같은 유저가 5번 동시 시도
        for (int i = 0; i < attemptCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();  // 모두 대기

                    reactionWriter.likeUp(
                            new UserIdentity(singleUser.getUserId()),
                            TargetType.JOB,
                            testJob.getJobId()
                    );

                    successCount.incrementAndGet();
                    doneLatch.countDown();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    doneLatch.countDown();
                }
            });
        }

        // 동시 시작!
        startLatch.countDown();

        // 모든 스레드 완료 대기
        doneLatch.await();
        executor.shutdown();

        // then - 1번만 성공, 나머지는 실패
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(4);

        // Job의 like_count가 1만 증가
        Job updatedJob = jobRepository.findById(new JobIdentity(testJob.getJobId()))
                .orElseThrow();
        assertThat(updatedJob.getPopularity().getLikeCount()).isEqualTo(1L);

        // Reaction이 1개만 생성
        List<Reaction> reactions = reactionRepository.findByTargetTypeAndTargetId(
                TargetType.JOB,
                testJob.getJobId()
        );
        assertThat(reactions).hasSize(1);

        // User의 likeGivenCount가 1
        User updatedUser = userRepository.findById(new UserIdentity(singleUser.getUserId()))
                .orElseThrow();
        assertThat(updatedUser.getLikeGivenCount()).isEqualTo(1L);
    }
}
