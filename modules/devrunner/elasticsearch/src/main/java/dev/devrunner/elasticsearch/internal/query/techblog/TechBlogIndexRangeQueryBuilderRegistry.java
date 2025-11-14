package dev.devrunner.elasticsearch.internal.query.techblog;

import dev.devrunner.elasticsearch.document.fieldSpec.techblog.TechBlogIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.BoolType;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder.RangeQueryBuilder;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class TechBlogIndexRangeQueryBuilderRegistry {

    private static final Map<TechBlogIndexField, RangeQueryBuilder> builderMap =
            new EnumMap<>(TechBlogIndexField.class);

    static {
        // Popularity 필드 (flattened)
        register(TechBlogIndexField.POPULARITY_VIEW_COUNT,    new RangeQueryBuilder(BoolType.FILTER));
        register(TechBlogIndexField.POPULARITY_COMMENT_COUNT, new RangeQueryBuilder(BoolType.FILTER));
        register(TechBlogIndexField.POPULARITY_LIKE_COUNT,    new RangeQueryBuilder(BoolType.FILTER));

        // 날짜 필드
        register(TechBlogIndexField.CREATED_AT, new RangeQueryBuilder(BoolType.FILTER));
        register(TechBlogIndexField.UPDATED_AT, new RangeQueryBuilder(BoolType.FILTER));
    }

    private static void register(TechBlogIndexField field, RangeQueryBuilder builder) {
        builderMap.put(field, builder);
    }

    public static Optional<RangeQueryBuilder> getBuilder(TechBlogIndexField field) {
        return Optional.ofNullable(builderMap.get(field));
    }

    public static final Function<TechBlogIndexField, Optional<RangeQueryBuilder>> LOOKUP =
            TechBlogIndexRangeQueryBuilderRegistry::getBuilder;
}
