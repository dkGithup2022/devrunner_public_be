package dev.devrunner.api.job;

import dev.devrunner.api.job.dto.JobRead;
import dev.devrunner.model.job.Job;
import dev.devrunner.model.job.JobIdentity;
import dev.devrunner.service.job.JobReader;
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
 * Job REST API 컨트롤러 (조회 전용)
 *
 * Job은 배치 크롤러를 통해 생성/수정되므로 조회 기능만 제공합니다.
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Job", description = "Job posting API (read-only)")
public class JobApiController {

    private final JobReader jobReader;

    /**
     * 채용공고 조회
     * GET /api/jobs/{jobId}
     */
    @Operation(summary = "Get job posting", description = "Retrieve a job posting by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{jobId}")
    public ResponseEntity<JobRead> getJob(
            @Parameter(description = "Job ID", example = "1") @PathVariable Long jobId) {
        log.info("GET /api/jobs/{}", jobId);

        Job job = jobReader.read(new JobIdentity(jobId));
        JobRead read = JobRead.from(job);

        log.info("Retrieved job - jobId: {}", read.getJobId());
        return ResponseEntity.ok(read);
    }
}
