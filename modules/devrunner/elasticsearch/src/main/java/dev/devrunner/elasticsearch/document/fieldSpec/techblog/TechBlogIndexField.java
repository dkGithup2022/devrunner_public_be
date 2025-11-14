package dev.devrunner.elasticsearch.document.fieldSpec.techblog;

import dev.devrunner.elasticsearch.document.fieldSpec.DocFieldName;
import dev.devrunner.elasticsearch.document.fieldSpec.DocQueryType;
import dev.devrunner.elasticsearch.internal.queryBuilder.FieldName;

public enum TechBlogIndexField implements DocFieldName, FieldName {
    DOC_ID("doc_id", DocQueryType.TERM),
    TECH_BLOG_ID("tech_blog_id", DocQueryType.TERM),
    URL("url", DocQueryType.TERM),

    COMPANY("company", DocQueryType.TERM),
    TITLE("title", DocQueryType.MATCH),

    ONE_LINER("one_liner", DocQueryType.MATCH),
    SUMMARY("summary", DocQueryType.MATCH),
    KOREAN_SUMMARY("korean_summary", DocQueryType.MATCH),
    MARKDOWN_BODY("markdown_body", DocQueryType.MATCH),

    THUMBNAIL_URL("thumbnail_url", DocQueryType.TERM),
    TECH_CATEGORIES("tech_categories", DocQueryType.TERM),
    ORIGINAL_URL("original_url", DocQueryType.TERM),

    POPULARITY_VIEW_COUNT("popularity_view_count", DocQueryType.RANGE),
    POPULARITY_COMMENT_COUNT("popularity_comment_count", DocQueryType.RANGE),
    POPULARITY_LIKE_COUNT("popularity_like_count", DocQueryType.RANGE),

    DELETED("deleted", DocQueryType.TERM),
    CREATED_AT("created_at", DocQueryType.RANGE),
    UPDATED_AT("updated_at", DocQueryType.RANGE),

    SEARCH_WORD("search_word", DocQueryType.MATCH);

    private final String fieldName;
    private final DocQueryType queryType;

    TechBlogIndexField(String fieldName, DocQueryType queryType) {
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
