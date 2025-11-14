package dev.devrunner.elasticsearch.internal.queryBuilder;


import co.elastic.clients.elasticsearch._types.query_dsl.Query;

public interface FieldQueryBuilder {
    Query build(String fieldName, String value);

    BoolType boolType();
}
