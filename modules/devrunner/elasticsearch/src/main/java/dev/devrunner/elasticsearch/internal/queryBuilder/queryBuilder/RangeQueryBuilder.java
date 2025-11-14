package dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder;


import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import dev.devrunner.elasticsearch.internal.queryBuilder.BoolType;

public class RangeQueryBuilder {
    private final BoolType boolType;

    public RangeQueryBuilder(BoolType boolType) {
        this.boolType = boolType;
    }

    public Query build(String fieldName, String gte, String lte) {
        var range = RangeQuery.of(r -> {
            RangeQuery.Builder builder = new RangeQuery.Builder().field(fieldName);
            if (gte != null) builder.gte(JsonData.of(gte));
            if (lte != null) builder.lte(JsonData.of(lte));
            return builder;
        });

        return Query.of(q -> q.range(range));
    }

    public BoolType boolType() {
        return boolType;
    }

}

