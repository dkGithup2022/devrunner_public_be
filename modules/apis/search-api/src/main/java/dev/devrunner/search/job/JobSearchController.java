package dev.devrunner.search.job;

import dev.devrunner.elasticsearch.api.job.JobSearch;
import dev.devrunner.elasticsearch.api.job.JobSearchResult;
import dev.devrunner.elasticsearch.api.similar.SimilarDocFinder;
import dev.devrunner.elasticsearch.document.fieldSpec.job.JobIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchCommand;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import dev.devrunner.search.job.dto.JobSearchRequest;
import dev.devrunner.search.job.dto.JobSearchResponse;
import dev.devrunner.search.techblog.dto.TechBlogSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Job Search API Controller
 */
@Tag(name = "Job Search", description = "채용 공고 검색 API")
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobSearchController {

    private final JobSearch jobSearch;
    private final SimilarDocFinder similarDocFinder;

    /**
     * Search jobs with filters
     *
     * @param request search conditions and filters
     * @return search results with pagination
     */
    @Operation(summary = "조건 기반 일반 검색")
    @PostMapping("/search")
    public ResponseEntity<JobSearchResponse> search(@RequestBody JobSearchRequest request) {
        log.info("🔍 Job search request: {}", request);

        // Build SearchCommand using Mapper
        List<SearchElement<JobIndexField>> elements = JobSearchRequestMapper.toElements(request);

        // is_closed=false 조건 명시적 추가 (비즈니스 로직)
        elements.add(new SearchElement<>(JobIndexField.IS_CLOSED, false));

        SearchCommand<JobIndexField> command = SearchCommand.of(
                elements,
                request.getFrom(),
                request.getTo()
        );

        // Execute search
        JobSearchResult result = jobSearch.search(command);

        // Convert response (JobDoc -> JobCard)
        JobSearchResponse response = JobSearchResponse.from(result);

        log.info("✅ Job search completed: found {} results", result.docs().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Find similar jobs for the given job
     *
     * @param docId document ID of the source job
     * @return list of similar jobs
     */
    @Operation(summary = "지정 채용 문서와 유사한 다른 채용 공고 추천")
    @GetMapping("/{docId}/similar/jobs")
    public ResponseEntity<JobSearchResponse> similarJobs(@PathVariable String docId) {
        log.info("🧭 Similar jobs search | docId: {}", docId);

        JobSearchResult result = similarDocFinder.findSimilarJobs(docId, 20);

        JobSearchResponse response = JobSearchResponse.from(result);

        log.info("✅ Similar jobs result | size: {}", response.getJobs().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Find similar tech blogs for the given job
     *
     * @param docId document ID of the source job
     * @return list of similar tech blogs
     */
    @Operation(summary = "지정 채용 문서와 유사한 기술 블로그 추천")
    @GetMapping("/{docId}/similar/techblogs")
    public ResponseEntity<TechBlogSearchResponse> similarTechBlogs(@PathVariable String docId) {
        log.info("🧭 Similar tech blogs search | docId: {}", docId);

        // 상위 20개 가져오기
        var result = similarDocFinder.findSimilarTechBlogsFromJob(docId, 20);

        // 상위 10개 선택 후 랜덤 섞기
        List<dev.devrunner.elasticsearch.document.TechBlogDoc> shuffled = result.docs().stream()
                .limit(10)
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        java.util.Collections.shuffle(shuffled);

        var shuffledResult = new dev.devrunner.elasticsearch.api.techblog.TechBlogSearchResult(shuffled, false);
        TechBlogSearchResponse response = TechBlogSearchResponse.from(shuffledResult);

        log.info("✅ Similar tech blogs result | size: {} (랜덤 정렬)", response.getTechBlogs().size());
        return ResponseEntity.ok(response);
    }
}
