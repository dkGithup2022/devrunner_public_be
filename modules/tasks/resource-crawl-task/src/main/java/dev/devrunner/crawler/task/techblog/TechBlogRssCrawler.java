package dev.devrunner.crawler.task.techblog;

import com.rometools.rome.feed.synd.SyndEntry;
import dev.devrunner.infra.techblog.repository.TechBlogRepository;
import dev.devrunner.model.common.TargetType;
import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.outbox.command.RecordOutboxEventCommand;
import dev.devrunner.outbox.recorder.OutboxEventRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 테크 블로그 RSS 크롤러
 *
 * Meta, Airbnb, Spotify 등 글로벌 기업과 무신사, 당근, 네이버 D2, 토스 등
 * 국내 기업의 엔지니어링 블로그 RSS를 크롤링합니다.
 * fetch 메서드는 RSS를 가져와서 TechBlog 리스트로 변환만 합니다.
 * run 메서드는 fetch + 중복 체크 + 저장을 수행합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TechBlogRssCrawler {

    private final RssFeedFetcher fetcher;
    private final RssToTechBlogConverter converter;
    private final TechBlogRepository repository;
    private final TechBlogPostProcessor postProcessor;

    private final OutboxEventRecorder outboxEventRecorder;

    // ===== Fetch 메서드들 (RSS 가져오기 - 저장 안 함) =====

    /**
     * Meta 엔지니어링 블로그 RSS 가져오기
     *
     * @return TechBlog 리스트 (저장되지 않음)
     */
    public List<TechBlog> fetchMeta() {
        return fetchFromRss("META", "https://engineering.fb.com/feed/");
    }

    /**
     * Airbnb 엔지니어링 블로그 RSS 가져오기
     *
     * @return TechBlog 리스트 (저장되지 않음)
     */
    public List<TechBlog> fetchAirbnb() {
        return fetchFromRss("AIRBNB", "https://medium.com/feed/airbnb-engineering");
    }


    /**
     * 무신사 기술 블로그 RSS 가져오기
     *
     * @return TechBlog 리스트 (저장되지 않음)
     */
    public List<TechBlog> fetchMusinsa() {
        return fetchFromRss("MUSINSA", "https://medium.com/feed/musinsa-tech");
    }

    /**
     * 당근 기술 블로그 RSS 가져오기
     *
     * @return TechBlog 리스트 (저장되지 않음)
     */
    public List<TechBlog> fetchKarrot() {
        return fetchFromRss("KARROT", "https://medium.com/feed/daangn");
    }

    /**
     * 네이버 D2 기술 블로그 Atom 피드 가져오기
     *
     * @return TechBlog 리스트 (저장되지 않음)
     */
    public List<TechBlog> fetchNaverD2() {
        return fetchFromRss("NAVER", "https://d2.naver.com/d2.atom");
    }

    /**
     * 토스 기술 블로그 RSS 가져오기
     *
     * @return TechBlog 리스트 (저장되지 않음)
     */
    public List<TechBlog> fetchToss() {
        return fetchFromRss("TOSS", "https://toss.tech/rss.xml");
    }

    // ===== Process 메서드들 (fetch + 후처리 - 저장 안 함) =====

    /**
     * Meta 엔지니어링 블로그 후처리 (저장 안 함)
     * 테스트용
     *
     * @return 후처리된 TechBlog 리스트
     */
    public List<TechBlog> processMeta() {
        log.info("Processing Meta blogs (without saving)...");
        List<TechBlog> blogs = fetchMeta();
        return blogs.stream()
                .map(postProcessor::process)
                .toList();
    }

    /**
     * Airbnb 엔지니어링 블로그 후처리 (저장 안 함)
     * 테스트용
     *
     * @return 후처리된 TechBlog 리스트
     */
    public List<TechBlog> processAirbnb() {
        log.info("Processing Airbnb blogs (without saving)...");
        List<TechBlog> blogs = fetchAirbnb();
        return blogs.stream()
                .map(postProcessor::process)
                .toList();
    }



    /**
     * 무신사 기술 블로그 후처리 (저장 안 함)
     * 테스트용
     *
     * @return 후처리된 TechBlog 리스트
     */
    public List<TechBlog> processMusinsa() {
        log.info("Processing Musinsa blogs (without saving)...");
        List<TechBlog> blogs = fetchMusinsa();
        return blogs.stream()
                .map(postProcessor::process)
                .toList();
    }

    /**
     * 당근 기술 블로그 후처리 (저장 안 함)
     * 테스트용
     *
     * @return 후처리된 TechBlog 리스트
     */
    public List<TechBlog> processKarrot() {
        log.info("Processing Karrot blogs (without saving)...");
        List<TechBlog> blogs = fetchKarrot();
        return blogs.stream()
                .map(postProcessor::process)
                .toList();
    }

    /**
     * 네이버 D2 기술 블로그 후처리 (저장 안 함)
     * 테스트용
     *
     * @return 후처리된 TechBlog 리스트
     */
    public List<TechBlog> processNaverD2() {
        log.info("Processing Naver D2 blogs (without saving)...");
        List<TechBlog> blogs = fetchNaverD2();
        return blogs.stream()
                .map(postProcessor::process)
                .toList();
    }

    /**
     * 토스 기술 블로그 후처리 (저장 안 함)
     * 테스트용
     *
     * @return 후처리된 TechBlog 리스트
     */
    public List<TechBlog> processToss() {
        log.info("Processing Toss blogs (without saving)...");
        List<TechBlog> blogs = fetchToss();
        return blogs.stream()
                .map(postProcessor::process)
                .toList();
    }

    // ===== Run 메서드들 (fetch + 저장) =====

    /**
     * Meta 엔지니어링 블로그 크롤링 및 저장
     */
    public void runMeta() {
        log.info("Starting Meta blog crawling...");
        List<TechBlog> blogs = fetchMeta();
        saveBlogs("META", blogs);
    }

    /**
     * Airbnb 엔지니어링 블로그 크롤링 및 저장
     */
    public void runAirbnb() {
        log.info("Starting Airbnb blog crawling...");
        List<TechBlog> blogs = fetchAirbnb();
        saveBlogs("AIRBNB", blogs);
    }


    /**
     * 무신사 기술 블로그 크롤링 및 저장
     */
    public void runMusinsa() {
        log.info("Starting Musinsa blog crawling...");
        List<TechBlog> blogs = fetchMusinsa();
        saveBlogs("MUSINSA", blogs);
    }

    /**
     * 당근 기술 블로그 크롤링 및 저장
     */
    public void runKarrot() {
        log.info("Starting Karrot blog crawling...");
        List<TechBlog> blogs = fetchKarrot();
        saveBlogs("KARROT", blogs);
    }

    /**
     * 네이버 D2 기술 블로그 크롤링 및 저장
     */
    public void runNaverD2() {
        log.info("Starting Naver D2 blog crawling...");
        List<TechBlog> blogs = fetchNaverD2();
        saveBlogs("NAVER", blogs);
    }

    /**
     * 토스 기술 블로그 크롤링 및 저장
     */
    public void runToss() {
        log.info("Starting Toss blog crawling...");
        List<TechBlog> blogs = fetchToss();
        saveBlogs("TOSS", blogs);
    }

    // ===== 내부 헬퍼 메서드 =====

    /**
     * RSS 피드에서 TechBlog 리스트 가져오기 (공통 로직)
     *
     * @param company 회사명
     * @param rssUrl  RSS 피드 URL
     * @return TechBlog 리스트
     */
    private List<TechBlog> fetchFromRss(String company, String rssUrl) {
        List<TechBlog> result = new ArrayList<>();

        try {
            log.info("Fetching RSS feed: company={}, url={}", company, rssUrl);
            List<SyndEntry> entries = fetcher.fetch(rssUrl);
            log.info("Fetched {} entries from {}", entries.size(), company);

            for (SyndEntry entry : entries) {
                try {
                    TechBlog blog = converter.convert(company, entry);
                    result.add(blog);
                } catch (Exception e) {
                    log.error("Failed to convert entry: company={}, url={}", company, entry.getLink(), e);
                }
            }

            log.info("Successfully converted {} blogs from {}", result.size(), company);

        } catch (Exception e) {
            log.error("Failed to fetch RSS feed: company={}, url={}", company, rssUrl, e);
        }

        return result;
    }

    /**
     * TechBlog 리스트를 DB에 저장 (중복 체크 + 후처리 포함)
     *
     * @param company 회사명
     * @param blogs   저장할 TechBlog 리스트
     */
    private void saveBlogs(String company, List<TechBlog> blogs) {
        int newCount = 0;
        int duplicateCount = 0;
        int postProcessFailCount = 0;

        for (TechBlog blog : blogs) {
            String url = blog.getUrl();

            // URL 중복 체크
            if (repository.findByUrl(url).isPresent()) {
                log.debug("Already exists: {}", url);
                duplicateCount++;
                continue;
            }

            // 후처리 (oneLiner, summary 생성)
            TechBlog processedBlog;
            try {
                log.info("Post-processing new blog: company={}, title={}", company, blog.getTitle());
                processedBlog = postProcessor.process(blog);
            } catch (Exception e) {
                log.error("Post-processing failed, saving original: company={}, url={}", company, url, e);
                processedBlog = blog;
                postProcessFailCount++;
            }

            // 저장
            try {
                var saved  = repository.save(processedBlog);
                newCount++;

                RecordOutboxEventCommand recordCommand = RecordOutboxEventCommand.updated(TargetType.TECH_BLOG, saved.getTechBlogId());
                outboxEventRecorder.record(recordCommand);
                log.info("Saved new blog: company={}, title={}", company, processedBlog.getTitle());
            } catch (Exception e) {
                log.error("Failed to save blog: company={}, url={}", company, url, e);
            }
        }

        log.info("Save completed: company={}, new={}, duplicate={}, postProcessFail={}, total={}",
                company, newCount, duplicateCount, postProcessFailCount, blogs.size());
    }
}
