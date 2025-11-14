package dev.devrunner.elasticsearch.internal.queryBuilder;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import dev.devrunner.elasticsearch.exception.SearchFieldNotFoundException;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryBuilder.RangeQueryBuilder;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryComposer.BoolQueryComposer;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryComposer.QueryWithBoolType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;



@Slf4j
public final class GenericSearchQueryBuilder {

    private GenericSearchQueryBuilder() {}

    // ✅ SearchCommand<F> 사용 버전
    public static <F extends FieldName> Query build(
            SearchCommand<F> command,
            Function<? super F, Optional<FieldQueryBuilder>> qbRegistry,
            Function<? super F, Optional<RangeQueryBuilder>> rangeRegistry
    ) {
        return build(command.conditions(), qbRegistry, rangeRegistry); // ← 여기!
    }

    // 요청 리스트 직접 전달 버전
    public static <F extends FieldName> Query build(
            List<SearchElement<F>> requests,
            Function<? super F, Optional<FieldQueryBuilder>> qbRegistry,
            Function<? super F, Optional<RangeQueryBuilder>> rangeRegistry
    ) {
        var queriesWithBool = new ArrayList<QueryWithBoolType>();

        for (var req : requests) {
            var field = req.getField();
            var value = req.getValue();
            var gte   = req.getGte();
            var lte   = req.getLte();

            var qOpt  = qbRegistry.apply(field);
            var rOpt  = rangeRegistry.apply(field);

            if (qOpt.isEmpty() && rOpt.isEmpty()) {
                throw new SearchFieldNotFoundException(field.getFieldName());
            }

            if (qOpt.isPresent()) {
                if (value == null) {
                    log.error("parameter value not assigned on text/term query. field={}", field);
                    continue;
                }
                var qb   = qOpt.get();
                var q    = qb.build(field.getFieldName(), value);
                var type = qb.boolType();
                queriesWithBool.add(new QueryWithBoolType(q, type));

            } else { // range
                if (gte == null && lte == null) {
                    log.error("both gte, lte not assigned on range query. field={}", field);
                    continue;
                }
                var rb   = rOpt.get();
                var q    = rb.build(field.getFieldName(), gte, lte);
                var type = rb.boolType();
                queriesWithBool.add(new QueryWithBoolType(q, type));
            }
        }

        return BoolQueryComposer.compose(queriesWithBool);
    }
}