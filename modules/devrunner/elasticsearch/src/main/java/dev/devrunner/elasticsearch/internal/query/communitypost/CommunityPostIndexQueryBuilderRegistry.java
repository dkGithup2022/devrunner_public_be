package dev.devrunner.elasticsearch.internal.query.communitypost;

import dev.devrunner.elasticsearch.document.fieldSpec.communitypost.CommunityPostIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.BoolType;
import dev.devrunner.elasticsearch.internal.queryBuilder.FieldQueryBuilder;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder.MatchQueryBuilder;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder.TermQueryBuilder;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class CommunityPostIndexQueryBuilderRegistry {
    private static final Map<CommunityPostIndexField, FieldQueryBuilder> builderMap =
            new EnumMap<>(CommunityPostIndexField.class);

    static {
        register(CommunityPostIndexField.DOC_ID, new TermQueryBuilder(BoolType.FILTER));
        register(CommunityPostIndexField.COMMUNITY_POST_ID, new TermQueryBuilder(BoolType.FILTER));
        register(CommunityPostIndexField.USER_ID, new TermQueryBuilder(BoolType.FILTER));

        register(CommunityPostIndexField.CATEGORY, new TermQueryBuilder(BoolType.FILTER));
        register(CommunityPostIndexField.TITLE, new MatchQueryBuilder(BoolType.SHOULD));
        register(CommunityPostIndexField.MARKDOWN_BODY, new MatchQueryBuilder(BoolType.SHOULD));

        register(CommunityPostIndexField.COMPANY, new TermQueryBuilder(BoolType.FILTER));
        register(CommunityPostIndexField.LOCATION, new TermQueryBuilder(BoolType.FILTER));
        register(CommunityPostIndexField.LINKED_JOB_ID, new TermQueryBuilder(BoolType.FILTER));
        register(CommunityPostIndexField.IS_FROM_JOB_COMMENT, new TermQueryBuilder(BoolType.FILTER));

        register(CommunityPostIndexField.DELETED, new TermQueryBuilder(BoolType.MUST));

        // Range 필드는 별도 처리:
        // POPULARITY_VIEW_COUNT, POPULARITY_COMMENT_COUNT, POPULARITY_LIKE_COUNT,
        // CREATED_AT, UPDATED_AT → RangeQueryBuilder
    }

    private static void register(CommunityPostIndexField field, FieldQueryBuilder builder) {
        builderMap.put(field, builder);
    }

    public static Optional<FieldQueryBuilder> getBuilder(CommunityPostIndexField field) {
        return Optional.ofNullable(builderMap.get(field));
    }

    public static final Function<CommunityPostIndexField, Optional<FieldQueryBuilder>> LOOKUP =
            CommunityPostIndexQueryBuilderRegistry::getBuilder;
}
