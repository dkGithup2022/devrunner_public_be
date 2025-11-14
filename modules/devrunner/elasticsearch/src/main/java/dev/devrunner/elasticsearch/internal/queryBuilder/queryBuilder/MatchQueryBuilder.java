package dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder;


import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import dev.devrunner.elasticsearch.internal.queryBuilder.BoolType;
import dev.devrunner.elasticsearch.internal.queryBuilder.FieldQueryBuilder;

public class MatchQueryBuilder implements FieldQueryBuilder {
    private final BoolType boolType;

    public MatchQueryBuilder(BoolType boolType) {
        this.boolType = boolType;
    }

    @Override
    public Query build(String fieldName, String value) {
        return Query.of(q -> q.match(m -> m.field(fieldName).query(value)));
    }

    @Override
    public BoolType boolType() {
        return boolType;
    }
}
