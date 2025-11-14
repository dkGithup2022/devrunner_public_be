package dev.devrunner.crawler.task.techblog;

import dev.devrunner.crawler.task.techblog.openai.TechBlogMdParser;
import dev.devrunner.crawler.task.techblog.openai.TechBlogOneLinerExtractor;
import dev.devrunner.crawler.task.techblog.openai.TechBlogSummarizer;
import dev.devrunner.crawler.task.techblog.openai.TechBlogKoreanSummaryTranslator;
import dev.devrunner.crawler.task.techblog.openai.TechBlogTechCategoryExtractor;
import dev.devrunner.model.common.TechCategory;
import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.openai.base.GptParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TechBlog 후처리 통합 프로세서
 *
 * RSS로 가져온 원본 TechBlog에 대해 GPT 기반 정보 추출을 수행합니다.
 *
 * 수행 단계:
 * 1. 전처리 (필요시 HTML → Markdown 변환)
 * 2. GPT로 정보 추출 (oneLiner, summary, techCategories)
 * 3. 한국어 번역
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TechBlogPostProcessor {

    private final TechBlogMdParser mdParser;
    private final TechBlogOneLinerExtractor oneLinerExtractor;
    private final TechBlogSummarizer summarizer;
    private final TechBlogKoreanSummaryTranslator koreanTranslator;
    private final TechBlogTechCategoryExtractor categoryExtractor;

    /**
     * TechBlog 후처리
     *
     * @param techBlog 원본 TechBlog (RSS에서 가져온 상태)
     * @return 후처리된 TechBlog (oneLiner, summary, techCategories 포함)
     */
    public TechBlog process(TechBlog techBlog) {
        log.info("Post-processing TechBlog: company={}, title={}",
                techBlog.getCompany(), techBlog.getTitle());

        try {
            String content = techBlog.getMarkdownBody();

            // 1. 필요시 HTML → Markdown 변환 (일부 RSS는 HTML 본문 제공)
            String mdContent = convertToMarkdownIfNeeded(content);

            // 2. oneLiner 생성
            String oneLiner = extractOneLiner(mdContent);

            // 3. summary 생성 (영어)
            String summary = extractSummary(mdContent);

            // 4. summary 한국어 번역
            String summaryKo = translateToKorean(summary);

            // 5. techCategories 추출
            List<TechCategory> techCategories = extractTechCategories(mdContent);

            // 6. 새로운 TechBlog 반환
            return new TechBlog(
                    techBlog.getTechBlogId(),
                    techBlog.getUrl(),
                    techBlog.getCompany(),
                    techBlog.getTitle(),
                    oneLiner,
                    summary,
                    summaryKo,
                    mdContent,
                    techBlog.getThumbnailUrl(),
                    techCategories,
                    techBlog.getOriginalUrl(),
                    techBlog.getPopularity(),
                    techBlog.getIsDeleted(),
                    techBlog.getCreatedAt(),
                    techBlog.getUpdatedAt()
            );

        } catch (Exception e) {
            log.error("Failed to post-process TechBlog: url={}", techBlog.getUrl(), e);
            return techBlog;
        }
    }

    /**
     * 필요시 HTML을 Markdown으로 변환
     *
     * @param content 원본 콘텐츠
     * @return Markdown 변환된 콘텐츠
     */
    private String convertToMarkdownIfNeeded(String content) {
        // HTML 태그가 있으면 변환 시도
        if (content != null && content.contains("<")) {
            try {
                log.debug("Converting HTML to Markdown");
                return mdParser.run(GptParams.ofMini(content));
            } catch (Exception e) {
                log.warn("Failed to convert HTML to Markdown, using original", e);
                return content;
            }
        }
        return content;
    }

    /**
     * 한 줄 소개 생성
     */
    private String extractOneLiner(String markdownBody) {
        try {
            var result = oneLinerExtractor.run(GptParams.ofMini(markdownBody));
            return result.oneLineSummary();
        } catch (Exception e) {
            log.error("Failed to extract oneLiner", e);
            return null;
        }
    }

    /**
     * 요약본 생성 (키워드 추출)
     */
    private String extractSummary(String markdownBody) {
        try {
            return summarizer.run(GptParams.ofMini(markdownBody));
        } catch (Exception e) {
            log.error("Failed to extract summary", e);
            return null;
        }
    }

    /**
     * 기술 카테고리 추출
     */
    private List<TechCategory> extractTechCategories(String markdownBody) {
        try {
            var result = categoryExtractor.run(GptParams.ofMini(markdownBody));
            return result.getTechCategoryEnums();
        } catch (Exception e) {
            log.error("Failed to extract tech categories", e);
            return List.of();
        }
    }

    /**
     * 영어 summary를 한국어로 번역
     */
    private String translateToKorean(String englishSummary) {
        if (englishSummary == null || englishSummary.isBlank()) {
            return null;
        }

        try {
            return koreanTranslator.run(GptParams.ofMini(englishSummary));
        } catch (Exception e) {
            log.error("Failed to translate summary to Korean", e);
            return null;
        }
    }
}
