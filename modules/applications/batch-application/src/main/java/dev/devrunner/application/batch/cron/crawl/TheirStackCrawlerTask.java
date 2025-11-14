package dev.devrunner.application.batch.cron.crawl;

import dev.devrunner.crawler.task.theirstack.TheirStackJobCrawler;
import dev.devrunner.crawler.task.theirstack.TheirStackJobGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.devrunner.application.batch.ScheduleUtils.executeBatchTask;

/**
 * TheirStack 크롤러 배치 스케줄러
 * <p>
 * Stage 1: TheirStack API로 채용 공고 데이터 수집 (회사별 독립 실행)
 * Stage 2: 수집된 데이터를 Job 엔티티로 변환 및 저장
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class TheirStackCrawlerTask {

    private final TheirStackJobCrawler crawler;
    private final TheirStackJobGenerator generator;

    // Stage 1: 회사별 크롤링 플래그
    private static final AtomicBoolean GOOGLE_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean META_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean NETFLIX_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean SPOTIFY_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean TIKTOK_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean APPLE_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean AMAZON_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean MICROSOFT_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean AIRBNB_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean UBER_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean STRIPE_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean SHOPIFY_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean GITLAB_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean TOSS_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean COUPANG_CRAWL_RUNNING = new AtomicBoolean(false);

    // Stage 2: Job 생성 플래그
    private static final AtomicBoolean JOB_GENERATE_RUNNING = new AtomicBoolean(false);

    // 로컬 테스트용 플래그
    private static final AtomicBoolean LOCAL_TEST_RUNNING = new AtomicBoolean(false);
    private int currentTaskIndex = 0;

    // ===== Stage 1: TheirStack 데이터 수집 =====

    /**
     * Google 채용 공고 수집
     * - 프로덕션: 3일마다 오전 9시 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 1분, 24시간마다
     */
    @Scheduled(cron = "0 0 9 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 60000, fixedDelay = 86400000)  // 로컬용: 1분 후, 24시간마다
    public void crawlGoogleJobs() {
        executeBatchTask(GOOGLE_CRAWL_RUNNING, "theirstack_crawl_google", crawler::runGoogle);
    }

    /**
     * Meta 채용 공고 수집
     * - 프로덕션: 3일마다 오전 9시 10분 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 2분, 24시간마다
     */
    @Scheduled(cron = "0 10 9 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 120000, fixedDelay = 86400000)  // 로컬용: 2분 후, 24시간마다
    public void crawlMetaJobs() {
        executeBatchTask(META_CRAWL_RUNNING, "theirstack_crawl_meta", crawler::runMeta);
    }

    /**
     * Netflix 채용 공고 수집
     * - 프로덕션: 3일마다 오전 9시 20분 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 3분, 24시간마다
     */
    @Scheduled(cron = "0 20 9 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 180000, fixedDelay = 86400000)  // 로컬용: 3분 후, 24시간마다
    public void crawlNetflixJobs() {
        executeBatchTask(NETFLIX_CRAWL_RUNNING, "theirstack_crawl_netflix", crawler::runNetflix);
    }

    /**
     * Spotify 채용 공고 수집
     * - 프로덕션: 3일마다 오전 9시 30분 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 4분, 24시간마다
     */
    @Scheduled(cron = "0 30 9 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 240000, fixedDelay = 86400000)  // 로컬용: 4분 후, 24시간마다
    public void crawlSpotifyJobs() {
        executeBatchTask(SPOTIFY_CRAWL_RUNNING, "theirstack_crawl_spotify", crawler::runSpotify);
    }

    /**
     * TikTok 채용 공고 수집
     * - 프로덕션: 3일마다 오전 9시 40분 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 5분, 24시간마다
     */
    @Scheduled(cron = "0 40 9 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 300000, fixedDelay = 86400000)  // 로컬용: 5분 후, 24시간마다
    public void crawlTikTokJobs() {
        executeBatchTask(TIKTOK_CRAWL_RUNNING, "theirstack_crawl_tiktok", crawler::runTikTok);
    }

    /**
     * Apple 채용 공고 수집
     * - 프로덕션: 3일마다 오전 9시 50분 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 6분, 24시간마다
     */
    @Scheduled(cron = "0 50 9 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 360000, fixedDelay = 86400000)  // 로컬용: 6분 후, 24시간마다
    public void crawlAppleJobs() {
        executeBatchTask(APPLE_CRAWL_RUNNING, "theirstack_crawl_apple", crawler::runApple);
    }

    /**
     * Amazon 채용 공고 수집
     * - 프로덕션: 3일마다 오전 10시 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 7분, 24시간마다
     */
    @Scheduled(cron = "0 0 10 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 420000, fixedDelay = 86400000)  // 로컬용: 7분 후, 24시간마다
    public void crawlAmazonJobs() {
        executeBatchTask(AMAZON_CRAWL_RUNNING, "theirstack_crawl_amazon", crawler::runAmazon);
    }

    /**
     * Microsoft 채용 공고 수집
     * - 프로덕션: 3일마다 오전 10시 10분 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 8분, 24시간마다
     */
    @Scheduled(cron = "0 10 10 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 480000, fixedDelay = 86400000)  // 로컬용: 8분 후, 24시간마다
    public void crawlMicrosoftJobs() {
        executeBatchTask(MICROSOFT_CRAWL_RUNNING, "theirstack_crawl_microsoft", crawler::runMicrosoft);
    }

    /**
     * Airbnb 채용 공고 수집
     * - 프로덕션: 3일마다 오전 10시 20분 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 9분, 24시간마다
     */
    @Scheduled(cron = "0 20 10 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 540000, fixedDelay = 86400000)  // 로컬용: 9분 후, 24시간마다
    public void crawlAirbnbJobs() {
        executeBatchTask(AIRBNB_CRAWL_RUNNING, "theirstack_crawl_airbnb", crawler::runAirbnb);
    }

    /**
     * Uber 채용 공고 수집
     * - 프로덕션: 3일마다 오전 10시 30분 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 10분, 24시간마다
     */
    @Scheduled(cron = "0 30 10 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 600000, fixedDelay = 86400000)  // 로컬용: 10분 후, 24시간마다
    public void crawlUberJobs() {
        executeBatchTask(UBER_CRAWL_RUNNING, "theirstack_crawl_uber", crawler::runUber);
    }

    /**
     * Stripe 채용 공고 수집
     * - 프로덕션: 3일마다 오전 10시 40분 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 11분, 24시간마다
     */
    @Scheduled(cron = "0 40 10 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 660000, fixedDelay = 86400000)  // 로컬용: 11분 후, 24시간마다
    public void crawlStripeJobs() {
        executeBatchTask(STRIPE_CRAWL_RUNNING, "theirstack_crawl_stripe", crawler::runStripe);
    }

    /**
     * Shopify 채용 공고 수집
     * - 프로덕션: 3일마다 오전 10시 50분 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 12분, 24시간마다
     */
    @Scheduled(cron = "0 50 10 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 720000, fixedDelay = 86400000)  // 로컬용: 12분 후, 24시간마다
    public void crawlShopifyJobs() {
        executeBatchTask(SHOPIFY_CRAWL_RUNNING, "theirstack_crawl_shopify", crawler::runShopify);
    }

    /**
     * Gitlab 채용 공고 수집
     * - 프로덕션: 3일마다 오전 11시 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 13분, 24시간마다
     */
    @Scheduled(cron = "0 0 11 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 780000, fixedDelay = 86400000)  // 로컬용: 13분 후, 24시간마다
    public void crawlGitlabJobs() {
        executeBatchTask(GITLAB_CRAWL_RUNNING, "theirstack_crawl_gitlab", crawler::runGitlab);
    }

    /**
     * Toss 채용 공고 수집
     * - 프로덕션: 3일마다 오전 11시 10분 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 14분, 24시간마다
     */
    @Scheduled(cron = "0 10 11 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 840000, fixedDelay = 86400000)  // 로컬용: 14분 후, 24시간마다
    public void crawlTossJobs() {
        executeBatchTask(TOSS_CRAWL_RUNNING, "theirstack_crawl_toss", crawler::runToss);
    }

    /**
     * Coupang 채용 공고 수집
     * - 프로덕션: 3일마다 오전 11시 20분 (매월 1,4,7,10,13,16,19,22,25,28,31일)
     * - 로컬: 시작 후 15분, 24시간마다
     */
    @Scheduled(cron = "0 20 11 */2 * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 900000, fixedDelay = 86400000)  // 로컬용: 15분 후, 24시간마다
    public void crawlCoupangJobs() {
        executeBatchTask(COUPANG_CRAWL_RUNNING, "theirstack_crawl_coupang", crawler::runCoupang);
    }

    // ===== 로컬 테스트용 임시 배치 =====

    /**
     * 로컬 테스트용: 모든 회사를 20초마다 순차적으로 1회씩만 크롤링
     * - 프로덕션 배포 시 반드시 주석 처리할 것
     */
  //  @Scheduled(fixedDelay = 20000)
    public void localTestCrawlAll() {
        if (currentTaskIndex >= 15) {
            log.info("=== All companies crawled. Test completed. ===");
            return;
        }

        executeBatchTask(LOCAL_TEST_RUNNING, "theirstack_local_test", () -> {
            log.info("=== Running task {}/15 ===", currentTaskIndex + 1);

            switch (currentTaskIndex) {
                case 0 -> crawler.runGoogle();
                case 1 -> crawler.runMeta();
                case 2 -> crawler.runNetflix();
                case 3 -> crawler.runSpotify();
                case 4 -> crawler.runTikTok();
                case 5 -> crawler.runApple();
                case 6 -> crawler.runAmazon();
                case 7 -> crawler.runMicrosoft();
                case 8 -> crawler.runAirbnb();
                case 9 -> crawler.runUber();
                case 10 -> crawler.runStripe();
                case 11 -> crawler.runShopify();
                case 12 -> crawler.runGitlab();
                case 13 -> crawler.runToss();
                case 14 -> crawler.runCoupang();
            }

            currentTaskIndex++;
        });
    }

    // ===== Stage 2: Job 생성 =====

    /**
     * TheirStack Job 생성
     * - crawl_theirstack_jobs (WAIT) → jobs 테이블
     * - 20초마다 1개씩 처리
     */
    @Scheduled(fixedDelay = 40000)
    public void generateTheirStackJobs() {
        executeBatchTask(JOB_GENERATE_RUNNING, "theirstack_generate_jobs", generator::run);
    }
}
