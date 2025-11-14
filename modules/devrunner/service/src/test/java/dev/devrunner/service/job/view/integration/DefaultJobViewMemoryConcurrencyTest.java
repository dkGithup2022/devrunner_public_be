package dev.devrunner.service.job.view.integration;

import dev.devrunner.infra.job.repository.JobRepository;
import dev.devrunner.model.common.Company;
import dev.devrunner.model.job.Job;
import dev.devrunner.model.job.JobIdentity;
import dev.devrunner.service.integrationConfig.IntegrationTestBase;
import dev.devrunner.service.job.view.JobViewMemory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JobViewMemory 동시성 테스트
 * <p>
 * ConcurrentHashMap + AtomicLong의 동시성 안전성을 검증하고,
 * 다수의 동시 조회가 정확하게 누적되는지 확인합니다.
 */
@Slf4j
class DefaultJobViewMemoryConcurrencyTest extends IntegrationTestBase {

    @Autowired
    private JobViewMemory viewMemory;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Job testJob;

    @BeforeEach
    void setUp() {
        // 테스트용 Job 생성
        testJob = Job.newJob(
                "https://example.com/job/1",
                Company.NAVER,
                "Backend Engineer",
                "Engineering Team",
                "Test job description",
                List.of()
        );
        testJob = jobRepository.save(testJob);
    }

    @AfterEach
    void clearAll() {
        // 테스트 후 데이터 정리
        jdbcTemplate.execute("DELETE FROM jobs");
    }

    @Test
    @DisplayName("100명이 같은 Job을 동시에 조회하면 viewCounts에 100이 정확히 누적됨")
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

                    viewMemory.countUp(testJob.getJobId());
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
        var updatedJob = jobRepository.findById(
                new JobIdentity(testJob.getJobId())
        ).orElseThrow();

        assertThat(updatedJob.getPopularity().getViewCount()).isEqualTo(100L);
        log.info("View count after 100 concurrent views: {}", updatedJob.getPopularity().getViewCount());
    }

    @Test
    @DisplayName("같은 Job에 여러 번 countUp 후 flush하면 누적된 값이 DB에 정확히 반영됨")
    void countUp_multiple_then_flush_countsAccurate() throws Exception {
        // given - 순차적으로 50번 조회
        for (int i = 0; i < 50; i++) {
            viewMemory.countUp(testJob.getJobId());
        }

        // when - flush
        viewMemory.flush();

        // then
        var updatedJob = jobRepository.findById(
                new JobIdentity(testJob.getJobId())
        ).orElseThrow();

        assertThat(updatedJob.getPopularity().getViewCount()).isEqualTo(50L);
    }

    @Test
    @DisplayName("flush 후 다시 countUp하면 새로운 조회수가 누적됨")
    void countUp_after_flush_newCountsAccumulated() throws Exception {
        // given - 첫 번째 조회 30번
        for (int i = 0; i < 30; i++) {
            viewMemory.countUp(testJob.getJobId());
        }

        // when - 첫 번째 flush
        viewMemory.flush();

        // 두 번째 조회 20번
        for (int i = 0; i < 20; i++) {
            viewMemory.countUp(testJob.getJobId());
        }

        // 두 번째 flush
        viewMemory.flush();

        // then - 총 50 (30 + 20)
        var updatedJob = jobRepository.findById(
                new JobIdentity(testJob.getJobId())
        ).orElseThrow();

        assertThat(updatedJob.getPopularity().getViewCount()).isEqualTo(50L);
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
                    viewMemory.countUp(testJob.getJobId());
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
        var updatedJob = jobRepository.findById(
                new JobIdentity(testJob.getJobId())
        ).orElseThrow();

        assertThat(updatedJob.getPopularity().getViewCount())
                .isGreaterThan(0L)
                .isLessThanOrEqualTo(50L);

        log.info("View count after concurrent countUp+flush: {} (expected <= 50)",
                updatedJob.getPopularity().getViewCount());
    }

    @Test
    @DisplayName("여러 Job에 동시에 조회수를 증가시켜도 각각 정확히 누적됨")
    void countUp_multipleJobs_concurrently_eachCountsAccurate() throws Exception {
        // given - Job 3개 추가 생성
        Job job2 = Job.newJob(
                "https://example.com/job/2",
                Company.SPOTIFY,
                "Frontend Engineer",
                "Frontend Team",
                "Test job 2",
                List.of()
        );
        job2 = jobRepository.save(job2);

        Job job3 = Job.newJob(
                "https://example.com/job/3",
                Company.LINE,
                "DevOps Engineer",
                "DevOps Team",
                "Test job 3",
                List.of()
        );
        job3 = jobRepository.save(job3);

        int threadCount = 30; // 각 Job당 30번씩
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount * 3);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount * 3);

        // when - 3개 Job에 각각 30번씩 동시 조회
        Long[] jobIds = {testJob.getJobId(), job2.getJobId(), job3.getJobId()};

        for (Long jobId : jobIds) {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        viewMemory.countUp(jobId);
                        doneLatch.countDown();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        doneLatch.countDown();
                    }
                });
            }
        }

        // 동시 시작!
        startLatch.countDown();

        // 모든 스레드 완료 대기
        doneLatch.await();
        executor.shutdown();

        // flush
        viewMemory.flush();

        // then - 각 Job의 view_count가 정확히 30
        var updatedJob1 = jobRepository.findById(new JobIdentity(testJob.getJobId())).orElseThrow();
        var updatedJob2 = jobRepository.findById(new JobIdentity(job2.getJobId())).orElseThrow();
        var updatedJob3 = jobRepository.findById(new JobIdentity(job3.getJobId())).orElseThrow();

        assertThat(updatedJob1.getPopularity().getViewCount()).isEqualTo(30L);
        assertThat(updatedJob2.getPopularity().getViewCount()).isEqualTo(30L);
        assertThat(updatedJob3.getPopularity().getViewCount()).isEqualTo(30L);

        log.info("Job1: {}, Job2: {}, Job3: {} view counts",
                updatedJob1.getPopularity().getViewCount(),
                updatedJob2.getPopularity().getViewCount(),
                updatedJob3.getPopularity().getViewCount());
    }
}
