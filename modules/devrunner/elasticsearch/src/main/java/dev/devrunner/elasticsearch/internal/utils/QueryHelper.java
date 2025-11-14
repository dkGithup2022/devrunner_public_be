package dev.devrunner.elasticsearch.internal.utils;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import java.util.ArrayList;
import java.util.List;

public class QueryHelper {

    /**
     * 기존 쿼리에 should 절을 병합
     * minimumShouldMatch는 0으로 설정 (should는 선택사항, 매칭 시 점수만 증가)
     */
    public static Query mergeShoulds(Query base, List<Query> shoulds) {
        if (shoulds == null || shoulds.isEmpty()) {
            return base;
        }

        if (base.isBool()) {
            var b = base.bool();
            var merged = new ArrayList<Query>();
            if (b.should() != null) {
                merged.addAll(b.should());
            }
            merged.addAll(shoulds);

            return Query.of(x -> x.bool(bb -> bb
                    .must(b.must())
                    .filter(b.filter())
                    .mustNot(b.mustNot())
                    .should(merged)
                    .minimumShouldMatch("0")
            ));
        } else {
            return Query.of(x -> x.bool(bb -> bb
                    .must(base)
                    .should(shoulds)
                    .minimumShouldMatch("0")
            ));
        }
    }

    /**
     * minimumShouldMatch를 직접 지정할 수 있는 버전
     */
    public static Query mergeShoulds(Query base, List<Query> shoulds, Integer minimumShouldMatch) {
        if (shoulds == null || shoulds.isEmpty()) {
            return base;
        }

        var minShould = minimumShouldMatch == null || minimumShouldMatch < 0
                ? "0"
                : String.valueOf(minimumShouldMatch);

        if (base.isBool()) {
            var b = base.bool();
            var merged = new ArrayList<Query>();
            if (b.should() != null) {
                merged.addAll(b.should());
            }
            merged.addAll(shoulds);

            return Query.of(x -> x.bool(bb -> bb
                    .must(b.must())
                    .filter(b.filter())
                    .mustNot(b.mustNot())
                    .should(merged)
                    .minimumShouldMatch(minShould)
            ));
        } else {
            return Query.of(x -> x.bool(bb -> bb
                    .must(base)
                    .should(shoulds)
                    .minimumShouldMatch(minShould)
            ));
        }
    }
}
