package dev.devrunner.elasticsearch.api.techblog;

import dev.devrunner.elasticsearch.document.JobDoc;
import dev.devrunner.elasticsearch.document.TechBlogDoc;

import java.util.List;

public record TechBlogSearchResult(
        List<TechBlogDoc> docs,
        boolean hasNext,
        long totalHits
) {
    // 2개 파라미터 받는 생성자 추가
    public TechBlogSearchResult(List<TechBlogDoc> docs, boolean hasNext) {
        this(docs, hasNext, -1L);
    }
}
