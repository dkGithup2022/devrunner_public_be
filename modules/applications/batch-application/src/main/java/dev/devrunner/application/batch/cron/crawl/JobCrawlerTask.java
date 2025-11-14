package dev.devrunner.application.batch.cron.crawl;

import dev.devrunner.crawler.task.job.contentCrawler.JobContentCrawler;
import dev.devrunner.crawler.task.job.contentGenerator.JobContentGenerator;
import dev.devrunner.crawler.task.job.datafix.RequiredExpFixTask;
import dev.devrunner.crawler.task.job.urlCrawler.GoogleJobCrawler;
import dev.devrunner.crawler.task.job.urlCrawler.KarrotJobCrawler;
import dev.devrunner.crawler.task.job.urlCrawler.LineJobCrawler;
import dev.devrunner.crawler.task.job.urlCrawler.MetaJobCrawler;
import dev.devrunner.crawler.task.job.urlCrawler.NaverJobCrawler;
import dev.devrunner.crawler.task.job.urlCrawler.NetflixJobCrawler;
import dev.devrunner.crawler.task.job.urlCrawler.SpotifyJobCrawler;
import dev.devrunner.crawler.task.job.urlCrawler.TiktokJobCrawler;
import dev.devrunner.crawler.task.job.urlCrawler.WoowahanJobCrawler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import static dev.devrunner.application.batch.ScheduleUtils.executeBatchTask;

@RequiredArgsConstructor
@Component
@Slf4j
public class JobCrawlerTask {

    private final NetflixJobCrawler netflixJobCrawler;
    private final SpotifyJobCrawler spotifyJobCrawler;
    private final TiktokJobCrawler tiktokJobCrawler;
    private final NaverJobCrawler naverJobCrawler;
    private final KarrotJobCrawler karrotJobCrawler;
    private final LineJobCrawler lineJobCrawler;
    private final WoowahanJobCrawler woowahanJobCrawler;
    private final JobContentCrawler jobContentCrawler;
    private final JobContentGenerator jobContentGenerator;


    private static final AtomicBoolean NETFLIX_URL_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean SPOTIFY_URL_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean TIKTOK_URL_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean NAVER_URL_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean KARROT_URL_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean LINE_URL_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean WOOWAHAN_URL_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean CONTENT_CRAWL_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean JOB_GENERATE_RUNNING = new AtomicBoolean(false);

    private static final AtomicBoolean DATA_FIX = new AtomicBoolean(false);

     /* Naver URL 수집
     * - 프로덕션: 매일 오후 9시 20분에 실행
     * - 로컬: 애플리케이션 시작 후 3분 뒤 1번만 실행
     */
    @Scheduled(cron = "0 20 21 * * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 180000, fixedDelay = 86400000)  // 로컬용: 3분 후, 24시간 후 재실행
    public void crawlNaverJobUrls() {
        executeBatchTask(NAVER_URL_CRAWL_RUNNING, "crawl_naver_job_urls", naverJobCrawler::run);
    }

    /**
     * Karrot(당근) URL 수집
     * - 프로덕션: 매일 오후 9시 30분에 실행
     * - 로컬: 애플리케이션 시작 후 4분 뒤 1번만 실행
     */
    @Scheduled(cron = "0 30 21 * * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 240000, fixedDelay = 86400000)  // 로컬용: 4분 후, 24시간 후 재실행
    public void crawlKarrotJobUrls() {
        executeBatchTask(KARROT_URL_CRAWL_RUNNING, "crawl_karrot_job_urls", karrotJobCrawler::run);
    }

    /**
     * Line URL 수집
     * - 프로덕션: 매일 오후 9시 40분에 실행
     * - 로컬: 애플리케이션 시작 후 5분 뒤 1번만 실행
     */
    @Scheduled(cron = "0 40 21 * * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 300000, fixedDelay = 86400000)  // 로컬용: 5분 후, 24시간 후 재실행
    public void crawlLineJobUrls() {
        executeBatchTask(LINE_URL_CRAWL_RUNNING, "crawl_line_job_urls", lineJobCrawler::run);
    }

    /**
     * Woowahan(배민) URL 수집
     * - 프로덕션: 매일 오후 9시 50분에 실행
     * - 로컬: 애플리케이션 시작 후 6분 뒤 1번만 실행
     */
    @Scheduled(cron = "0 50 21 * * ?")  // 프로덕션용
    //@Scheduled(initialDelay = 360000, fixedDelay = 86400000)  // 로컬용: 6분 후, 24시간 후 재실행
    public void crawlWoowahanJobUrls() {
        executeBatchTask(WOOWAHAN_URL_CRAWL_RUNNING, "crawl_woowahan_job_urls", woowahanJobCrawler::run);
    }

    /**
     * 콘텐츠 크롤링: 20초마다 실행
     */
    @Scheduled(fixedDelay = 20000)
    public void crawlJobContent() {
        executeBatchTask(CONTENT_CRAWL_RUNNING, "crawl_job_content", jobContentCrawler::run);
    }

    /**
     * Job 생성: 20초마다 실행
     */
    @Scheduled(fixedDelay = 20000)
    public void generateJob() {
        executeBatchTask(JOB_GENERATE_RUNNING, "generate_job", jobContentGenerator::run);
    }
}
