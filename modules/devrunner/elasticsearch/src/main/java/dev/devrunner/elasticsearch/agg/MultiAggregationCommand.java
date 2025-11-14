package dev.devrunner.elasticsearch.agg;

import dev.devrunner.elasticsearch.internal.queryBuilder.FieldName;

import java.util.List;

/**
 * 여러 집계 쿼리를 한 번에 실행하는 명령
 * <p>
 * 각 쿼리는 독립적인 필터 조건을 가지며,
 * 한 번의 Elasticsearch 요청으로 모든 결과를 받습니다.
 */
public record MultiAggregationCommand<F extends FieldName>(
        List<AggregationQuery<F>> queries
) {
    public MultiAggregationCommand {
        queries = (queries == null) ? List.of() : List.copyOf(queries);
        if (queries.isEmpty()) {
            throw new IllegalArgumentException("At least one query is required");
        }
    }

    public static <F extends FieldName> MultiAggregationCommand<F> of(
            List<AggregationQuery<F>> queries
    ) {
        return new MultiAggregationCommand<>(queries);
    }
}
