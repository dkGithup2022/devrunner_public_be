package dev.devrunner.elasticsearch.internal.query.job;


import dev.devrunner.elasticsearch.document.fieldSpec.job.JobIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.BoolType;
import dev.devrunner.elasticsearch.internal.queryBuilder.FieldQueryBuilder;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder.MatchQueryBuilder;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder.TermQueryBuilder;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class JobIndexQueryBuilderRegistry {
    private static final Map<JobIndexField, FieldQueryBuilder> builderMap =
            new EnumMap<>(JobIndexField.class);

    static {
        register(JobIndexField.DOC_ID, new TermQueryBuilder(BoolType.FILTER));
        register(JobIndexField.JOB_ID, new TermQueryBuilder(BoolType.FILTER));
        register(JobIndexField.URL, new TermQueryBuilder(BoolType.FILTER));

        register(JobIndexField.COMPANY, new TermQueryBuilder(BoolType.FILTER));
        register(JobIndexField.TITLE, new MatchQueryBuilder(BoolType.SHOULD));
        register(JobIndexField.ORGANIZATION, new TermQueryBuilder(BoolType.FILTER));
        register(JobIndexField.ONE_LINE_SUMMARY, new MatchQueryBuilder(BoolType.SHOULD));

        register(JobIndexField.CAREER_LEVEL, new TermQueryBuilder(BoolType.FILTER));
        register(JobIndexField.EXPERIENCE_REQUIRED, new TermQueryBuilder(BoolType.FILTER));

        register(JobIndexField.EMPLOYMENT_TYPE, new TermQueryBuilder(BoolType.FILTER));
        register(JobIndexField.POSITION_CATEGORY, new TermQueryBuilder(BoolType.FILTER));
        register(JobIndexField.REMOTE_POLICY, new TermQueryBuilder(BoolType.FILTER));
        register(JobIndexField.TECH_CATEGORIES, new TermQueryBuilder(BoolType.FILTER));

        register(JobIndexField.IS_OPEN_ENDED, new TermQueryBuilder(BoolType.FILTER));
        register(JobIndexField.IS_CLOSED, new TermQueryBuilder(BoolType.FILTER));

        register(JobIndexField.LOCATIONS, new TermQueryBuilder(BoolType.FILTER));

        register(JobIndexField.FULL_DESCRIPTION, new MatchQueryBuilder(BoolType.SHOULD));

        register(JobIndexField.HAS_ASSIGNMENT, new TermQueryBuilder(BoolType.FILTER));
        register(JobIndexField.HAS_CODING_TEST, new TermQueryBuilder(BoolType.FILTER));
        register(JobIndexField.HAS_LIVE_CODING, new TermQueryBuilder(BoolType.FILTER));

        register(JobIndexField.COMPENSATION_CURRENCY, new TermQueryBuilder(BoolType.FILTER));
        register(JobIndexField.COMPENSATION_UNIT, new TermQueryBuilder(BoolType.FILTER));
        register(JobIndexField.COMPENSATION_HAS_STOCK_OPTION, new TermQueryBuilder(BoolType.FILTER));

        register(JobIndexField.DELETED, new TermQueryBuilder(BoolType.MUST));

        // Range 필드는 별도 처리:
        // MIN_YEARS, MAX_YEARS, STARTED_AT, ENDED_AT, INTERVIEW_COUNT, INTERVIEW_DAYS,
        // COMPENSATION_MIN_BASE_PAY, COMPENSATION_MAX_BASE_PAY,
        // POPULARITY_VIEW_COUNT, POPULARITY_COMMENT_COUNT, POPULARITY_LIKE_COUNT,
        // CREATED_AT, UPDATED_AT → RangeQueryBuilder
    }

    private static void register(JobIndexField field, FieldQueryBuilder builder) {
        builderMap.put(field, builder);
    }

    public static Optional<FieldQueryBuilder> getBuilder(JobIndexField field) {
        return Optional.ofNullable(builderMap.get(field));
    }

    public static final Function<JobIndexField, Optional<FieldQueryBuilder>> LOOKUP =
            JobIndexQueryBuilderRegistry::getBuilder;
}
