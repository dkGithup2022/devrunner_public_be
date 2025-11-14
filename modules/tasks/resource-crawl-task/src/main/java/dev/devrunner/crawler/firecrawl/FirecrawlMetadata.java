package dev.devrunner.crawler.firecrawl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FirecrawlMetadata(
        String title,
        String description,
        String language,
        String keywords,
        String robots,
        String ogTitle,
        String ogDescription,
        String ogUrl,
        String ogImage,
        String ogSiteName,
        String sourceURL,
        Integer statusCode
) {
}
