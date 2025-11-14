package dev.devrunner.search.techblog;

import dev.devrunner.elasticsearch.api.techblog.TechBlogSearch;
import dev.devrunner.elasticsearch.api.techblog.TechBlogSearchResult;
import dev.devrunner.elasticsearch.document.fieldSpec.techblog.TechBlogIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchCommand;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import dev.devrunner.search.techblog.dto.TechBlogSearchRequest;
import dev.devrunner.search.techblog.dto.TechBlogSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TechBlog Search API Controller
 */
@Tag(name = "TechBlog Search", description = "기술 블로그 검색 API")
@RestController
@RequestMapping("/api/techblogs")
@RequiredArgsConstructor
@Slf4j
public class TechBlogSearchController {

    private final TechBlogSearch techBlogSearch;

    /**
     * Search tech blogs with filters
     *
     * @param request search conditions and filters
     * @return search results with pagination
     */
    @Operation(summary = "조건 기반 기술 블로그 검색")
    @PostMapping("/search")
    public ResponseEntity<TechBlogSearchResponse> search(@RequestBody TechBlogSearchRequest request) {
        log.info("🔍 TechBlog search request: {}", request);

        // Build SearchCommand using Mapper
        List<SearchElement<TechBlogIndexField>> elements = TechBlogSearchRequestMapper.toElements(request);
        SearchCommand<TechBlogIndexField> command = SearchCommand.of(
                elements,
                request.getFrom(),
                request.getTo()
        );

        // Execute search
        TechBlogSearchResult result = techBlogSearch.search(command);

        // Convert response (TechBlogDoc -> TechBlogCard)
        TechBlogSearchResponse response = TechBlogSearchResponse.from(result);

        log.info("✅ TechBlog search completed: found {} results", result.docs().size());
        return ResponseEntity.ok(response);
    }
}
