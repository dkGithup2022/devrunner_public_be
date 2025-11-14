package dev.devrunner.elasticsearch.internal.query.communitypost;

import dev.devrunner.elasticsearch.document.fieldSpec.communitypost.CommunityPostIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.BoolType;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder.RangeQueryBuilder;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class CommunityPostIndexRangeQueryBuilderRegistry {

    private static final Map<CommunityPostIndexField, RangeQueryBuilder> builderMap =
            new EnumMap<>(CommunityPostIndexField.class);

    static {
        // Popularity 필드 (flattened)
        register(CommunityPostIndexField.POPULARITY_VIEW_COUNT,    new RangeQueryBuilder(BoolType.FILTER));
        register(CommunityPostIndexField.POPULARITY_COMMENT_COUNT, new RangeQueryBuilder(BoolType.FILTER));
        register(CommunityPostIndexField.POPULARITY_LIKE_COUNT,    new RangeQueryBuilder(BoolType.FILTER));

        // 날짜 필드
        register(CommunityPostIndexField.CREATED_AT, new RangeQueryBuilder(BoolType.FILTER));
        register(CommunityPostIndexField.UPDATED_AT, new RangeQueryBuilder(BoolType.FILTER));
    }

    private static void register(CommunityPostIndexField field, RangeQueryBuilder builder) {
        builderMap.put(field, builder);
    }

    public static Optional<RangeQueryBuilder> getBuilder(CommunityPostIndexField field) {
        return Optional.ofNullable(builderMap.get(field));
    }

    public static final Function<CommunityPostIndexField, Optional<RangeQueryBuilder>> LOOKUP =
            CommunityPostIndexRangeQueryBuilderRegistry::getBuilder;
}
