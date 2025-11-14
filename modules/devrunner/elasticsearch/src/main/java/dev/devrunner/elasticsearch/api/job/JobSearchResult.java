package dev.devrunner.elasticsearch.api.job;


import dev.devrunner.elasticsearch.document.JobDoc;

import java.util.List;

public record JobSearchResult(
        List<JobDoc> docs,
        boolean hasNext,
        // -1 means it doesn't use total count
        long totalHits
) {
    // 2개 파라미터 받는 생성자 추가
    public JobSearchResult(List<JobDoc> docs, boolean hasNext) {
        this(docs, hasNext, -1L);
    }
}

