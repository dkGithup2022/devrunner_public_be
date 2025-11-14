package dev.devrunner.elasticsearch.internal.query.job;


import dev.devrunner.elasticsearch.document.fieldSpec.job.JobIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.BoolType;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder.RangeQueryBuilder;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class JobIndexRangeQueryBuilderRegistry {

    private static final Map<JobIndexField, RangeQueryBuilder> builderMap =
            new EnumMap<>(JobIndexField.class);

    static {
        // 숫자/날짜 Range 필드 등록
        register(JobIndexField.MIN_YEARS,        new RangeQueryBuilder(BoolType.FILTER));
        register(JobIndexField.MAX_YEARS,        new RangeQueryBuilder(BoolType.FILTER));
        register(JobIndexField.STARTED_AT,       new RangeQueryBuilder(BoolType.FILTER));
        register(JobIndexField.ENDED_AT,         new RangeQueryBuilder(BoolType.FILTER));
        register(JobIndexField.INTERVIEW_COUNT,  new RangeQueryBuilder(BoolType.FILTER));
        register(JobIndexField.INTERVIEW_DAYS,   new RangeQueryBuilder(BoolType.FILTER));
        register(JobIndexField.CREATED_AT,       new RangeQueryBuilder(BoolType.FILTER));
        register(JobIndexField.UPDATED_AT,       new RangeQueryBuilder(BoolType.FILTER));

        // Compensation 필드 (flattened)
        register(JobIndexField.COMPENSATION_MIN_BASE_PAY, new RangeQueryBuilder(BoolType.FILTER));
        register(JobIndexField.COMPENSATION_MAX_BASE_PAY, new RangeQueryBuilder(BoolType.FILTER));

        // Popularity 필드 (flattened)
        register(JobIndexField.POPULARITY_VIEW_COUNT,    new RangeQueryBuilder(BoolType.FILTER));
        register(JobIndexField.POPULARITY_COMMENT_COUNT, new RangeQueryBuilder(BoolType.FILTER));
        register(JobIndexField.POPULARITY_LIKE_COUNT,    new RangeQueryBuilder(BoolType.FILTER));
    }

    private static void register(JobIndexField field, RangeQueryBuilder builder) {
        builderMap.put(field, builder);
    }

    public static Optional<RangeQueryBuilder> getBuilder(JobIndexField field) {
        return Optional.ofNullable(builderMap.get(field));
    }

    public static final Function<JobIndexField, Optional<RangeQueryBuilder>> LOOKUP =
            JobIndexRangeQueryBuilderRegistry::getBuilder;
}