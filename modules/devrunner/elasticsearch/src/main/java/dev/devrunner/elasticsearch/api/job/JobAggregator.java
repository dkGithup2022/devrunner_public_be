package dev.devrunner.elasticsearch.api.job;

import dev.devrunner.elasticsearch.agg.*;
import dev.devrunner.elasticsearch.document.fieldSpec.job.JobIndexField;
import dev.devrunner.elasticsearch.internal.query.job.JobIndexQueryBuilderRegistry;
import dev.devrunner.elasticsearch.internal.query.job.JobIndexRangeQueryBuilderRegistry;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Job 집계 실행기
 * <p>
 * AggregationRequest 리스트를 받아 Elasticsearch 집계 쿼리를 실행합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JobAggregator {
    private final MultiAggregationExecutor aggregationExecutor;

    @Value("${elasticsearch.index.job}")
    private String JOB_INDEX;

    /**
     * 여러 집계 요청을 한 번에 실행
     *
     * @param requests 집계 요청 리스트
     * @return 집계 결과
     */
    public MultiAggregationResult aggregate(List<AggregationRequest<JobIndexField>> requests) {
        log.debug("Executing {} aggregation requests for Job index", requests.size());

        // AggregationRequest -> AggregationQuery 변환
        List<AggregationQuery<JobIndexField>> queries = requests.stream()
                .map(this::toAggregationQuery)
                .toList();

        var command = MultiAggregationCommand.of(queries);

        return aggregationExecutor.aggregateMulti(
                JOB_INDEX,
                command,
                JobIndexQueryBuilderRegistry.LOOKUP,
                JobIndexRangeQueryBuilderRegistry.LOOKUP
        );
    }

    /**
     * AggregationRequest를 AggregationQuery로 변환
     */
    private AggregationQuery<JobIndexField> toAggregationQuery(
            AggregationRequest<JobIndexField> request
    ) {
        // SearchCommand의 conditions를 집계 필터로 사용
        @SuppressWarnings("unchecked")
        List<SearchElement<JobIndexField>> conditions =
                (List<SearchElement<JobIndexField>>) request.searchCommand().conditions();

        // BucketAggregation 생성
        BucketAggregation<JobIndexField> bucket = switch (request.bucketType()) {
            case TERMS -> BucketAggregation.terms(
                    request.bucketField(),
                    request.queryName() + "_bucket",
                    request.size() != null ? request.size() : 10,
                    request.metrics()
            );
            case DATE_HISTOGRAM -> BucketAggregation.dateHistogram(
                    request.bucketField(),
                    request.queryName() + "_bucket",
                    request.interval() != null ? request.interval() : "1d",
                    request.metrics()
            );
            default -> throw new IllegalArgumentException("Unsupported bucket type: " + request.bucketType());
        };

        return AggregationQuery.bucket(
                request.queryName(),
                conditions,
                bucket
        );
    }
}
