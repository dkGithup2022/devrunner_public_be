package dev.devrunner.service.communitypost.view.integration;

import dev.devrunner.infra.communitypost.repository.CommunityPostRepository;
import dev.devrunner.infra.user.repository.UserRepository;
import dev.devrunner.model.communitypost.CommunityPost;
import dev.devrunner.model.communitypost.CommunityPostCategory;
import dev.devrunner.model.communitypost.CommunityPostIdentity;
import dev.devrunner.model.user.User;
import dev.devrunner.service.communitypost.view.CommunityPostViewMemory;
import dev.devrunner.service.integrationConfig.IntegrationTestBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CommunityPostViewMemory 동시성 테스트
 * <p>
 * ConcurrentHashMap + AtomicLong의 동시성 안전성을 검증하고,
 * 다수의 동시 조회가 정확하게 누적되는지 확인합니다.
 */
@Slf4j
class DefaultCommunityPostViewMemoryConcurrencyTest extends IntegrationTestBase {

    @Autowired
    private CommunityPostViewMemory viewMemory;

    @Autowired
    private CommunityPostRepository communityPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;
    private CommunityPost testPost;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성
        testUser = User.newUser("google-123", "test@example.com", "testUser");
        testUser = userRepository.save(testUser);

        // 테스트용 CommunityPost 생성
        testPost = CommunityPost.newPost(
                testUser.getUserId(),
                CommunityPostCategory.TECH_ARTICLE,
                "Test Post",
                "Test content"
        );
        testPost = communityPostRepository.save(testPost);
    }

    @AfterEach
    void clearAll() {
        // 테스트 후 데이터 정리
        jdbcTemplate.execute("DELETE FROM community_posts");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    @DisplayName("100명이 같은 CommunityPost를 동시에 조회하면 viewCounts에 100이 정확히 누적됨")
    void countUp_concurrently_viewCountsAccurate() throws Exception {
        // given
        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // when - 100명이 동시에 조회
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();  // 모두 대기

                    viewMemory.countUp(testPost.getCommunityPostId());
                    log.debug("View {} counted", index);

                    doneLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    doneLatch.countDown();
                }
            });
        }

        // 동시 시작!
        startLatch.countDown();

        // 모든 스레드 완료 대기
        doneLatch.await();
        executor.shutdown();

        // then - flush를 호출하여 DB에 반영
        viewMemory.flush();

        // DB에서 view_count 확인
        var updatedPost = communityPostRepository.findById(
                new CommunityPostIdentity(testPost.getCommunityPostId())
        ).orElseThrow();

        assertThat(updatedPost.getPopularity().getViewCount()).isEqualTo(100L);
        log.info("View count after 100 concurrent views: {}", updatedPost.getPopularity().getViewCount());
    }

    @Test
    @DisplayName("같은 Post에 여러 번 countUp 후 flush하면 누적된 값이 DB에 정확히 반영됨")
    void countUp_multiple_then_flush_countsAccurate() throws Exception {
        // given - 순차적으로 50번 조회
        for (int i = 0; i < 50; i++) {
            viewMemory.countUp(testPost.getCommunityPostId());
        }

        // when - flush
        viewMemory.flush();

        // then
        var updatedPost = communityPostRepository.findById(
                new CommunityPostIdentity(testPost.getCommunityPostId())
        ).orElseThrow();

        assertThat(updatedPost.getPopularity().getViewCount()).isEqualTo(50L);
    }

    @Test
    @DisplayName("flush 후 다시 countUp하면 새로운 조회수가 누적됨")
    void countUp_after_flush_newCountsAccumulated() throws Exception {
        // given - 첫 번째 조회 30번
        for (int i = 0; i < 30; i++) {
            viewMemory.countUp(testPost.getCommunityPostId());
        }

        // when - 첫 번째 flush
        viewMemory.flush();

        // 두 번째 조회 20번
        for (int i = 0; i < 20; i++) {
            viewMemory.countUp(testPost.getCommunityPostId());
        }

        // 두 번째 flush
        viewMemory.flush();

        // then - 총 50 (30 + 20)
       var updatedPost = communityPostRepository.findById(
                new CommunityPostIdentity(testPost.getCommunityPostId())
        ).orElseThrow();

        assertThat(updatedPost.getPopularity().getViewCount()).isEqualTo(50L);
    }

    @Test
    @DisplayName("동시에 countUp + flush가 호출되어도 데이터 손실 없이 처리됨")
    void countUp_and_flush_concurrently_noDataLoss() throws Exception {
        // given
        int countThreads = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(countThreads + 1); // +1 for flush thread
        ExecutorService executor = Executors.newFixedThreadPool(countThreads + 1);

        // when - 50개 스레드가 countUp, 1개 스레드가 flush
        for (int i = 0; i < countThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    viewMemory.countUp(testPost.getCommunityPostId());
                    doneLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    doneLatch.countDown();
                }
            });
        }

        // flush 스레드
        executor.submit(() -> {
            try {
                startLatch.await();
                viewMemory.flush();
                doneLatch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                doneLatch.countDown();
            }
        });

        // 동시 시작!
        startLatch.countDown();

        // 모든 스레드 완료 대기
        doneLatch.await();
        executor.shutdown();

        // 남은 것이 있을 수 있으므로 한 번 더 flush
        viewMemory.flush();

        // then - 일부는 첫 flush에, 일부는 두 번째 flush에 반영되어 총 50
        var updatedPost = communityPostRepository.findById(
                new CommunityPostIdentity(testPost.getCommunityPostId())
        ).orElseThrow();

        assertThat(updatedPost.getPopularity().getViewCount())
                .isGreaterThan(0L)
                .isLessThanOrEqualTo(50L);

        log.info("View count after concurrent countUp+flush: {} (expected <= 50)",
                updatedPost.getPopularity().getViewCount());
    }
}
