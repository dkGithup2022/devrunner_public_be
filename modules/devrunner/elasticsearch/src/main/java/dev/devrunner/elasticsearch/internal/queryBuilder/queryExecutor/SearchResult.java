package dev.devrunner.elasticsearch.internal.queryBuilder.queryExecutor;

import java.util.List;

/**
 * Elasticsearch 검색 결과 wrapper
 *
 * @param docs 검색된 문서 목록
 * @param totalHits 전체 결과 개수 (필터 조건에 맞는 전체 문서 수)
 */
public record SearchResult<T>(
    List<T> docs,
    long totalHits
) {
}
