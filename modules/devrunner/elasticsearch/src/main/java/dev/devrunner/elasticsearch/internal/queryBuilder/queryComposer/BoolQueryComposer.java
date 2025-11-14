package dev.devrunner.elasticsearch.internal.queryBuilder.queryComposer;


import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import dev.devrunner.elasticsearch.internal.queryBuilder.BoolType;

import java.util.List;

public class BoolQueryComposer {

    public static Query compose(List<QueryWithBoolType> queries) {
        return Query.of(
                q -> q.bool(
                        b -> {
                            for (QueryWithBoolType qbt : queries) {
                                switch (qbt.type()) {
                                    case MUST -> b.must(qbt.query());
                                    case SHOULD -> b.should(qbt.query());
                                    case FILTER -> b.filter(qbt.query());
                                    case MUST_NOT -> b.mustNot(qbt.query());
                                }
                            }
                            // 선택사항: 최소 조건 설정 (예: SHOULD 조건이 있을 경우)
                            long shouldCount = queries.stream().filter(bq -> bq.type() == BoolType.SHOULD).count();
                            if (shouldCount > 0) {
                                b.minimumShouldMatch(String.valueOf(1)); // 최소 하나는 매칭
                            }
                            return b;
                        }));
    }
}
