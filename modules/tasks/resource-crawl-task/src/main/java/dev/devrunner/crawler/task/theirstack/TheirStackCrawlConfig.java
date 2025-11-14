package dev.devrunner.crawler.task.theirstack;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * TheirStack 크롤링 설정
 * <p>
 * 회사별로 다른 검색 조건을 설정하기 위한 Config
 */
@Value
@Builder
public class TheirStackCrawlConfig {
    String company;
    List<String> countryCodes;
    List<String> jobTitlePatterns;
    int postedAtMaxAgeDays;
    int limit;

    /**
     * 기본 설정 (한국, 엔지니어/개발자, 3일 이내, 100개 제한)
     */
    public static TheirStackCrawlConfig defaultConfig(String company) {
        return TheirStackCrawlConfig.builder()
                .company(company)
                .countryCodes(List.of("KR"))
                .jobTitlePatterns(List.of("(?i)engineer", "(?i)developer", "(?i)개발자", "(?i)엔지니어"))
                .postedAtMaxAgeDays(3)
                .limit(20)
                .build();
    }

    /**
     * 커스텀 설정
     */
    public static TheirStackCrawlConfig customConfig(
            String company,
            List<String> countryCodes,
            List<String> jobTitlePatterns,
            int postedAtMaxAgeDays,
            int limit
    ) {
        return TheirStackCrawlConfig.builder()
                .company(company)
                .countryCodes(countryCodes)
                .jobTitlePatterns(jobTitlePatterns)
                .postedAtMaxAgeDays(postedAtMaxAgeDays)
                .limit(limit)
                .build();
    }
}
