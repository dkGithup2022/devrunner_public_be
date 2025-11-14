package dev.devrunner.elasticsearch.internal.query.techblog;

import dev.devrunner.elasticsearch.document.fieldSpec.techblog.TechBlogIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.BoolType;
import dev.devrunner.elasticsearch.internal.queryBuilder.FieldQueryBuilder;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder.MatchQueryBuilder;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder.TermQueryBuilder;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class TechBlogIndexQueryBuilderRegistry {
    private static final Map<TechBlogIndexField, FieldQueryBuilder> builderMap =
            new EnumMap<>(TechBlogIndexField.class);

    static {
        register(TechBlogIndexField.DOC_ID, new TermQueryBuilder(BoolType.FILTER));
        register(TechBlogIndexField.TECH_BLOG_ID, new TermQueryBuilder(BoolType.FILTER));
        register(TechBlogIndexField.URL, new TermQueryBuilder(BoolType.FILTER));

        register(TechBlogIndexField.COMPANY, new TermQueryBuilder(BoolType.FILTER));
        register(TechBlogIndexField.TITLE, new MatchQueryBuilder(BoolType.SHOULD));

        register(TechBlogIndexField.ONE_LINER, new MatchQueryBuilder(BoolType.SHOULD));
        register(TechBlogIndexField.SUMMARY, new MatchQueryBuilder(BoolType.SHOULD));
        register(TechBlogIndexField.KOREAN_SUMMARY, new MatchQueryBuilder(BoolType.SHOULD));  // 추가
        register(TechBlogIndexField.SEARCH_WORD, new MatchQueryBuilder(BoolType.SHOULD));     // 추가
        register(TechBlogIndexField.MARKDOWN_BODY, new MatchQueryBuilder(BoolType.SHOULD));

        register(TechBlogIndexField.THUMBNAIL_URL, new TermQueryBuilder(BoolType.FILTER));
        register(TechBlogIndexField.TECH_CATEGORIES, new TermQueryBuilder(BoolType.FILTER));
        register(TechBlogIndexField.ORIGINAL_URL, new TermQueryBuilder(BoolType.FILTER));

        register(TechBlogIndexField.DELETED, new TermQueryBuilder(BoolType.MUST));

        // Range 필드는 별도 처리:
        // POPULARITY_VIEW_COUNT, POPULARITY_COMMENT_COUNT, POPULARITY_LIKE_COUNT,
        // CREATED_AT, UPDATED_AT → RangeQueryBuilder
    }

    private static void register(TechBlogIndexField field, FieldQueryBuilder builder) {
        builderMap.put(field, builder);
    }

    public static Optional<FieldQueryBuilder> getBuilder(TechBlogIndexField field) {
        return Optional.ofNullable(builderMap.get(field));
    }

    public static final Function<TechBlogIndexField, Optional<FieldQueryBuilder>> LOOKUP =
            TechBlogIndexQueryBuilderRegistry::getBuilder;
}
