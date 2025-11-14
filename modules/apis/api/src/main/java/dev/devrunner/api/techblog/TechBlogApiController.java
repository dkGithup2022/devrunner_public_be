package dev.devrunner.api.techblog;

import dev.devrunner.api.techblog.dto.TechBlogRead;
import dev.devrunner.model.techblog.TechBlog;
import dev.devrunner.model.techblog.TechBlogIdentity;
import dev.devrunner.service.techblog.TechBlogReader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TechBlog REST API 컨트롤러 (조회 전용)
 *
 * TechBlog는 배치 크롤러를 통해 생성/수정되므로 조회 기능만 제공합니다.
 */
@RestController
@RequestMapping("/api/tech-blogs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tech Blog", description = "Tech blog API (read-only)")
public class TechBlogApiController {

    private final TechBlogReader techBlogReader;

    /**
     * 기술 블로그 조회
     * GET /api/tech-blogs/{techBlogId}
     */
    @Operation(summary = "Get tech blog", description = "Retrieve a tech blog by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "404", description = "Tech blog not found")
    })
    @GetMapping("/{techBlogId}")
    public ResponseEntity<TechBlogRead> getTechBlog(
            @Parameter(description = "Tech blog ID", example = "1") @PathVariable Long techBlogId) {
        log.info("GET /api/tech-blogs/{}", techBlogId);

        TechBlog techBlog = techBlogReader.read(new TechBlogIdentity(techBlogId));
        TechBlogRead read = TechBlogRead.from(techBlog);

        log.info("Retrieved tech blog - techBlogId: {}", read.getTechBlogId());
        return ResponseEntity.ok(read);
    }
}
