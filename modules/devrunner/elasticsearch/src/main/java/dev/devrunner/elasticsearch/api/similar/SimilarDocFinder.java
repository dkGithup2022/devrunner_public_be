package dev.devrunner.elasticsearch.api.similar;

import dev.devrunner.elasticsearch.api.job.JobSearch;
import dev.devrunner.elasticsearch.api.job.JobSearchResult;


import dev.devrunner.elasticsearch.api.techblog.TechBlogSearch;
import dev.devrunner.elasticsearch.api.techblog.TechBlogSearchResult;
import dev.devrunner.elasticsearch.document.DocBase;
import dev.devrunner.elasticsearch.document.JobDoc;
import dev.devrunner.elasticsearch.document.TechBlogDoc;
import dev.devrunner.elasticsearch.document.fieldSpec.job.JobIndexField;
import dev.devrunner.elasticsearch.document.fieldSpec.techblog.TechBlogIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchCommand;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import dev.devrunner.elasticsearch.mapper.JobDocMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SimilarDocFinder - 유사 문서 검색
 * <p>
 * RRF(Reciprocal Rank Fusion)를 사용하여 Vector 검색과 BM25 검색 결과를 융합
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SimilarDocFinder {

    private final JobSearch jobSearch;
    private final TechBlogSearch techBlogSearch;

    /**
     * Job -> 유사한 Job들 찾기
     * - Vector 검색: Job의 vector 사용
     * - BM25 검색: Job의 oneLineSummary 사용
     */
    public JobSearchResult findSimilarJobs(String articleId, int size) {
        // 1. docId로 현재 Job 조회
        JobDoc sourceDoc = jobSearch.getById(JobDoc.docId(articleId));

        // 2. vector로 KNN 검색
        List<JobDoc> vectorResults = jobSearch.searchByVector(
                sourceDoc.getVector(),
                new SearchCommand<>(List.of(), 0, size * 2),
                size * 2
        ).docs();

        String queryText = buildQueryText( sourceDoc);
        // 3. oneLineSummary로 BM25 검색
        SearchCommand<JobIndexField> bm25Command = new SearchCommand<>(
                List.of(new SearchElement<>(JobIndexField.FULL_DESCRIPTION, queryText)),
                0,
                size * 2
        );
        List<JobDoc> bm25Results = jobSearch.search(bm25Command).docs();

        // 4. RRF로 두 결과 병합
        List<JobDoc> merged = applyRRF(vectorResults, bm25Results, size + 1);

        // 5. 자기 자신 제외
        List<JobDoc> filtered = merged.stream()
                .filter(doc -> !doc.getDocId().equals(JobDoc.docId(articleId)))
                .limit(size)
                .toList();

        //
        return new JobSearchResult(filtered, false);
    }

    /**
     * Job -> 유사한 TechBlog들 찾기
     * - Vector 검색: Job의 vector 사용
     * - BM25 검색: Job의 title + techCategories + fullDescription(500자)로 TechBlog의 summary 필드 검색
     */
    public TechBlogSearchResult findSimilarTechBlogsFromJob(String articleId, int size) {
        // 1. docId로 Job 조회
        JobDoc jobDoc = jobSearch.getById(JobDoc.docId(articleId));

        // 2. Job의 vector로 TechBlog KNN 검색
        List<TechBlogDoc> vectorResults = techBlogSearch.searchByVector(
                jobDoc.getVector(),
                new SearchCommand<>(List.of(), 0, size * 2),
                size * 2
        ).docs();

        // 3. Job의 title + techCategories + fullDescription(500자)로 BM25 검색
        String queryText = buildQueryText(jobDoc);
        SearchCommand<TechBlogIndexField> bm25Command = new SearchCommand<>(
                List.of(new SearchElement<>(TechBlogIndexField.SEARCH_WORD, queryText)),
                0,
                size * 2
        );
        List<TechBlogDoc> bm25Results = techBlogSearch.search(bm25Command).docs();

        // 4. RRF로 두 결과 병합
        List<TechBlogDoc> merged = applyRRF(vectorResults, bm25Results, size);

        return new TechBlogSearchResult(merged, false);
    }

    /**
     * Job 정보로부터 검색 쿼리 텍스트 생성
     * - title: 직무 제목
     * - techCategories: 기술 카테고리들
     * - fullDescription: 최대 500자까지
     */
    private String buildQueryText(JobDoc jobDoc) {
        StringBuilder query = new StringBuilder();

        // 1. 제목
        if (jobDoc.getTitle() != null) {
            query.append(jobDoc.getTitle()).append(" ");
        }

        // 2. 기술 카테고리
        if (jobDoc.getTechCategories() != null && !jobDoc.getTechCategories().isEmpty()) {
            query.append(String.join(" ", jobDoc.getTechCategories())).append(" ");
        }

        // 3. 상세 설명 (최대 500자)
        if (jobDoc.getFullDescription() != null) {
            String desc = jobDoc.getFullDescription();
            if (desc.length() > 500) {
                desc = desc.substring(0, 500);
            }
            query.append(desc);
        }

        return query.toString().trim();
    }

    /**
     * RRF (Reciprocal Rank Fusion) 알고리즘
     * - K=60 상수 사용
     * - score = 1/(K + rank)
     * - 두 검색 결과의 점수를 합산하여 정렬
     */
    private <T extends DocBase> List<T> applyRRF(List<T> vectorResults, List<T> bm25Results, int topK) {
        final int K = 60; // RRF constant

        // docId -> RRF score 계산
        Map<String, Double> rrfScores = new HashMap<>();
        Map<String, T> docMap = new HashMap<>();

        // Vector 검색 결과의 순위 기반 점수
        for (int i = 0; i < vectorResults.size(); i++) {
            T doc = vectorResults.get(i);
            String docId = doc.getDocId();
            double score = 1.0 / (K + i + 1); // rank는 0부터 시작하므로 +1
            rrfScores.put(docId, rrfScores.getOrDefault(docId, 0.0) + score);
            docMap.put(docId, doc);
        }

        // BM25 검색 결과의 순위 기반 점수
        for (int i = 0; i < bm25Results.size(); i++) {
            T doc = bm25Results.get(i);
            String docId = doc.getDocId();
            double score = 1.0 / (K + i + 1);
            rrfScores.put(docId, rrfScores.getOrDefault(docId, 0.0) + score);
            docMap.put(docId, doc);
        }

        // RRF 점수로 정렬하여 상위 topK개 반환
        return rrfScores.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())) // 내림차순
                .limit(topK)
                .map(entry -> docMap.get(entry.getKey()))
                .collect(Collectors.toList());
    }
}
