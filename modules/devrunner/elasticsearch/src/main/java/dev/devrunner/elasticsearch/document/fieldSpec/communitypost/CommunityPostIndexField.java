package dev.devrunner.elasticsearch.document.fieldSpec.communitypost;

import dev.devrunner.elasticsearch.document.fieldSpec.DocFieldName;
import dev.devrunner.elasticsearch.document.fieldSpec.DocQueryType;
import dev.devrunner.elasticsearch.internal.queryBuilder.FieldName;

public enum CommunityPostIndexField implements DocFieldName, FieldName {
    DOC_ID("doc_id", DocQueryType.TERM),
    COMMUNITY_POST_ID("community_post_id", DocQueryType.TERM),
    USER_ID("user_id", DocQueryType.TERM),

    CATEGORY("category", DocQueryType.TERM),
    TITLE("title", DocQueryType.MATCH),
    MARKDOWN_BODY("markdown_body", DocQueryType.MATCH),

    COMPANY("company", DocQueryType.TERM),
    LOCATION("location", DocQueryType.TERM),
    LINKED_JOB_ID("linked_job_id", DocQueryType.TERM),
    IS_FROM_JOB_COMMENT("is_from_job_comment", DocQueryType.TERM),

    POPULARITY_VIEW_COUNT("popularity_view_count", DocQueryType.RANGE),
    POPULARITY_COMMENT_COUNT("popularity_comment_count", DocQueryType.RANGE),
    POPULARITY_LIKE_COUNT("popularity_like_count", DocQueryType.RANGE),

    DELETED("deleted", DocQueryType.TERM),
    CREATED_AT("created_at", DocQueryType.RANGE),
    UPDATED_AT("updated_at", DocQueryType.RANGE);

    private final String fieldName;
    private final DocQueryType queryType;

    CommunityPostIndexField(String fieldName, DocQueryType queryType) {
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
