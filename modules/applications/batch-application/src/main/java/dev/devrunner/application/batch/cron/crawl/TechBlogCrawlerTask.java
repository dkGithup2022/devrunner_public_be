package dev.devrunner.application.batch.cron.crawl;

import dev.devrunner.crawler.task.techblog.TechBlogRssCrawler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import static dev.devrunner.application.batch.ScheduleUtils.executeBatchTask;

@RequiredArgsConstructor
@Component
@Slf4j
public class TechBlogCrawlerTask {

    private final TechBlogRssCrawler techBlogRssCrawler;

    private static final AtomicBoolean META_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean AIRBNB_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean SPOTIFY_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean MUSINSA_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean KARROT_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean NAVER_D2_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean TOSS_CRAWL_RUNNING = new AtomicBoolean(false);

    /**
     * Meta 테크블로그 크롤링
     * - 프로덕션: 매일 오후 7시 30분에 실행
     * - 로컬: 애플리케이션 시작 직후 1번만 실행
     */
    @Scheduled(cron = "0 30 15 * * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 0, fixedDelay = 86400000)  // 로컬용: 시작 즉시, 24시간 후 재실행
    public void crawlMetaTechBlog() {
        executeBatchTask(META_CRAWL_RUNNING, "crawl_meta_techblog", techBlogRssCrawler::runMeta);
    }

    /**
     * Airbnb 테크블로그 크롤링
     * - 프로덕션: 매일 오후 7시 40분에 실행
     * - 로컬: 애플리케이션 시작 후 5분 뒤 1번만 실행
     */
    @Scheduled(cron = "0 40 15 * * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 300000, fixedDelay = 86400000)  // 로컬용: 5분 후, 24시간 후 재실행
    public void crawlAirbnbTechBlog() {
        executeBatchTask(AIRBNB_CRAWL_RUNNING, "crawl_airbnb_techblog", techBlogRssCrawler::runAirbnb);
    }

    /**
     * Spotify 테크블로그 크롤링
     * - 프로덕션: 매일 오후 7시 50분에 실행
     * - 로컬: 애플리케이션 시작 후 10분 뒤 1번만 실행
     */
    @Scheduled(cron = "0 50 15 * * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 600000, fixedDelay = 86400000)  // 로컬용: 10분 후, 24시간 후 재실행
    public void crawlSpotifyTechBlog() {
        executeBatchTask(SPOTIFY_CRAWL_RUNNING, "crawl_spotify_techblog", techBlogRssCrawler::runSpotify);
    }

    /**
     * 무신사 테크블로그 크롤링
     * - 프로덕션: 매일 오후 8시에 실행
     * - 로컬: 애플리케이션 시작 후 3분 뒤 실행
     */
 //   @Scheduled(cron = "0 0 16 * * ?")  // 프로덕션용
    @Scheduled(initialDelay = 180000, fixedDelay = 86400000)  // 로컬용: 3분 후, 24시간 후 재실행
    public void crawlMusinsaTechBlog() {
        executeBatchTask(MUSINSA_CRAWL_RUNNING, "crawl_musinsa_techblog", techBlogRssCrawler::runMusinsa);
    }

    /**
     * 당근 테크블로그 크롤링
     * - 프로덕션: 매일 오후 8시 10분에 실행
     * - 로컬: 애플리케이션 시작 후 3분 30초 뒤 실행
     */
  //  @Scheduled(cron = "0 10 16 * * ?")  // 프로덕션용
    @Scheduled(initialDelay = 210000, fixedDelay = 86400000)  // 로컬용: 3분 30초 후, 24시간 후 재실행
    public void crawlKarrotTechBlog() {
        executeBatchTask(KARROT_CRAWL_RUNNING, "crawl_karrot_techblog", techBlogRssCrawler::runKarrot);
    }

    /**
     * 네이버 D2 테크블로그 크롤링
     * - 프로덕션: 매일 오후 8시 20분에 실행
     * - 로컬: 애플리케이션 시작 후 4분 뒤 실행
     */
    //@Scheduled(cron = "0 20 16 * * ?")  // 프로덕션용
    @Scheduled(initialDelay = 240000, fixedDelay = 86400000)  // 로컬용: 4분 후, 24시간 후 재실행
    public void crawlNaverD2TechBlog() {
        executeBatchTask(NAVER_D2_CRAWL_RUNNING, "crawl_naver_d2_techblog", techBlogRssCrawler::runNaverD2);
    }

    /**
     * 토스 테크블로그 크롤링
     * - 프로덕션: 매일 오후 8시 30분에 실행
     * - 로컬: 애플리케이션 시작 후 4분 30초 뒤 실행
     */
   // @Scheduled(cron = "0 30 16 * * ?")  // 프로덕션용
    @Scheduled(initialDelay = 270000, fixedDelay = 86400000)  // 로컬용: 4분 30초 후, 24시간 후 재실행
    public void crawlTossTechBlog() {
        executeBatchTask(TOSS_CRAWL_RUNNING, "crawl_toss_techblog", techBlogRssCrawler::runToss);
    }
}
