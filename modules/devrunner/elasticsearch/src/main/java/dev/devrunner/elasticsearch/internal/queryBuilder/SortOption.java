package dev.devrunner.elasticsearch.internal.queryBuilder;

import co.elastic.clients.elasticsearch._types.SortOrder;

public record SortOption(String field, SortOrder order) {
    public static SortOption desc(String field) {
        return new SortOption(field, SortOrder.Desc);
    }

    public static SortOption asc(String field) {
        return new SortOption(field, SortOrder.Asc);
    }
}