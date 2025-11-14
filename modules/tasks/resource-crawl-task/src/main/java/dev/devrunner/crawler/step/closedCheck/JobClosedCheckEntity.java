package dev.devrunner.crawler.step.closedCheck;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Job 마감 체크 이력 엔티티
 * <p>
 * open 상태의 Job에 대해 마감 여부를 주기적으로 확인하고 이력을 저장합니다.
 */
@Table("job_closed_checks")
@Getter
@AllArgsConstructor
public class JobClosedCheckEntity {
    @Id
    private Long id;
    private Long jobId;              // jobs 테이블 참조
    private String url;              // 체크한 URL
    private Boolean closed;          // 마감 여부
    private String closedReason;     // 마감 이유 (EXPIRED, CLOSED, NOT_HIRING, CANNOT_READ_PAGE)
    private String explanation;      // 상세 설명
    private Instant checkedAt;       // 체크 시각

    /**
     * 새로운 체크 이력 생성
     *
     * @param jobId         Job ID
     * @param url           체크한 URL
     * @param closed        마감 여부
     * @param closedReason  마감 이유
     * @param explanation   상세 설명
     * @return JobClosedCheckEntity
     */
    public static JobClosedCheckEntity create(
            Long jobId,
            String url,
            Boolean closed,
            String closedReason,
            String explanation
    ) {
        return new JobClosedCheckEntity(
                null,
                jobId,
                url,
                closed,
                closedReason,
                explanation,
                Instant.now()
        );
    }
}
