package dev.devrunner.crawler.firecrawl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FirecrawlResponse(
        boolean success,
        FirecrawlData data
) {
}
