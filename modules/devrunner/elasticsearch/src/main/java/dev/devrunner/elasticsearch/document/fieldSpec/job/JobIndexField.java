package dev.devrunner.elasticsearch.document.fieldSpec.job;


import dev.devrunner.elasticsearch.document.fieldSpec.DocFieldName;
import dev.devrunner.elasticsearch.document.fieldSpec.DocQueryType;
import dev.devrunner.elasticsearch.internal.queryBuilder.FieldName;

public enum JobIndexField implements DocFieldName, FieldName {
    DOC_ID("doc_id", DocQueryType.TERM),
    JOB_ID("job_id", DocQueryType.TERM),
    URL("url", DocQueryType.TERM),

    COMPANY("company", DocQueryType.TERM),
    TITLE("title", DocQueryType.MATCH),
    ORGANIZATION("organization", DocQueryType.TERM),
    ONE_LINE_SUMMARY("one_line_summary", DocQueryType.MATCH),

    MIN_YEARS("min_years", DocQueryType.RANGE),
    MAX_YEARS("max_years", DocQueryType.RANGE),
    EXPERIENCE_REQUIRED("experience_required", DocQueryType.TERM),
    CAREER_LEVEL("career_level", DocQueryType.TERM),

    EMPLOYMENT_TYPE("employment_type", DocQueryType.TERM),
    POSITION_CATEGORY("position_category", DocQueryType.TERM),
    REMOTE_POLICY("remote_policy", DocQueryType.TERM),
    TECH_CATEGORIES("tech_categories", DocQueryType.TERM),

    STARTED_AT("started_at", DocQueryType.RANGE),
    ENDED_AT("ended_at", DocQueryType.RANGE),
    IS_OPEN_ENDED("is_open_ended", DocQueryType.TERM),
    IS_CLOSED("is_closed", DocQueryType.TERM),

    LOCATIONS("locations", DocQueryType.TERM),

    FULL_DESCRIPTION("full_description", DocQueryType.MATCH),

    HAS_ASSIGNMENT("has_assignment", DocQueryType.TERM),
    HAS_CODING_TEST("has_coding_test", DocQueryType.TERM),
    HAS_LIVE_CODING("has_live_coding", DocQueryType.TERM),
    INTERVIEW_COUNT("interview_count", DocQueryType.RANGE),
    INTERVIEW_DAYS("interview_days", DocQueryType.RANGE),

    COMPENSATION_MIN_BASE_PAY("compensation_min_base_pay", DocQueryType.RANGE),
    COMPENSATION_MAX_BASE_PAY("compensation_max_base_pay", DocQueryType.RANGE),
    COMPENSATION_CURRENCY("compensation_currency", DocQueryType.TERM),
    COMPENSATION_UNIT("compensation_unit", DocQueryType.TERM),
    COMPENSATION_HAS_STOCK_OPTION("compensation_has_stock_option", DocQueryType.TERM),

    POPULARITY_VIEW_COUNT("popularity_view_count", DocQueryType.RANGE),
    POPULARITY_COMMENT_COUNT("popularity_comment_count", DocQueryType.RANGE),
    POPULARITY_LIKE_COUNT("popularity_like_count", DocQueryType.RANGE),

    DELETED("deleted", DocQueryType.TERM),
    CREATED_AT("created_at", DocQueryType.RANGE),
    UPDATED_AT("updated_at", DocQueryType.RANGE);

    private final String fieldName;
    private final DocQueryType queryType;

    JobIndexField(String fieldName, DocQueryType queryType) {
        this.fieldName = fieldName;
        this.queryType = queryType;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    public DocQueryType getQueryType() {
        return queryType;
    }
}