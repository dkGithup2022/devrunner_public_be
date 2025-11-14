package dev.devrunner.elasticsearch.api.job.boost;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.json.JsonData;
import dev.devrunner.elasticsearch.document.JobDocKeywordBoost;
import dev.devrunner.elasticsearch.document.fieldSpec.job.JobIndexField;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchCommand;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryExecutor.SearchQueryExecutor;
import dev.devrunner.elasticsearch.internal.queryBuilder.queryExecutor.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultJobDocBoostQueryHelper implements JobDocBoostQueryHelper {

    @Value("${elasticsearch.index.job_keyword_boost}")
    private String JOB_BOOST_INDEX;

    private static final int BOOST_RULE_LIMIT = 5;

    private final SearchQueryExecutor searchQueryExecutor;

    @Override
    public boolean hasSearchWord(SearchCommand<JobIndexField> command) {
        return !extractTextForPercolate(command).isBlank();
    }

    @Override
    public List<JobDocKeywordBoost> extractBoostKeywords(SearchCommand<JobIndexField> command) {
        String text = extractTextForPercolate(command);
        if (text.isBlank()) {
            return List.of();
        }

        // 1) percolate 쿼리
        Query percolate = Query.of(q -> q.percolate(p -> p
                .field("q")
                .document(JsonData.of(Map.of("text", text)))
        ));

        var query = Query.of(q -> q.bool(b -> b.must(percolate)));

        log.info("Percolate query: {}", query.toString());

        SearchResult<JobDocKeywordBoost> searchResult;
        try {
            searchResult = searchQueryExecutor.search(
                    JOB_BOOST_INDEX,
                    query,
                    0,
                    BOOST_RULE_LIMIT,
                    JobDocKeywordBoost.class
            );

            log.info("Percolate hits: {}", searchResult.docs());

        } catch (Exception e) {
            log.error("Percolate 실패: {}", e.getMessage(), e);
            // percolate 실패 시 기본 검색만 진행하도록 폴백
            return List.of();
        }

        List<JobDocKeywordBoost> hits = searchResult.docs();

        // 2) 정제/정렬/중복 제거
        //  - canonical 비어있으면 제외
        //  - priority desc → canonical 길이 desc
        //  - canonical 기준 distinct (동일 canonical 여러 개면 priority 높은 것 채택)
        return hits.stream()
                .filter(d -> d != null && d.getCanonical() != null && !d.getCanonical().isBlank())
                .sorted(Comparator
                        .comparing(JobDocKeywordBoost::getPriority, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(d -> d.getCanonical().length(), Comparator.reverseOrder())
                )
                .collect(Collectors.toMap(
                        JobDocKeywordBoost::getCanonical,
                        d -> d,
                        (a, b) -> { // 충돌 시 priority 높은 쪽
                            Integer pa = a.getPriority(), pb = b.getPriority();
                            if (pa == null && pb == null) return a;
                            if (pa == null) return b;
                            if (pb == null) return a;
                            return pa >= pb ? a : b;
                        },
                        LinkedHashMap::new
                ))
                .values().stream()
                .limit(BOOST_RULE_LIMIT)
                .toList();
    }

    @Override
    public List<Query> buildBoostQueries(SearchCommand<JobIndexField> command) {
        List<JobDocKeywordBoost> rules = extractBoostKeywords(command);
        if (rules.isEmpty()) {
            return List.of();
        }

        log.info("Boost rules: {}", rules);
        List<Query> shoulds = new ArrayList<>(rules.size());
        for (JobDocKeywordBoost r : rules) {
            double b = r.getBoost() == null ? 1.0 : r.getBoost();
            // title^2, full_description 에 phrase 매칭 (slop=0)
            Query q = Query.of(x -> x.multiMatch(mm -> mm
                    .query(r.getCanonical())
                    .type(TextQueryType.Phrase)
                    .slop(0)
                    .fields(
                        JobIndexField.TITLE.getFieldName() + "^2",
                        JobIndexField.FULL_DESCRIPTION.getFieldName()
                    )
                    .boost((float) b)
            ));
            shoulds.add(q);
        }

        log.info("Boost should queries: {}", shoulds);
        return shoulds;
    }

    /**
     * TITLE / FULL_DESCRIPTION 의 value를 공백 결합 (중복은 Set으로 제거)
     */
    private String extractTextForPercolate(SearchCommand<JobIndexField> cmd) {
        Set<String> bag = new LinkedHashSet<>();
        for (var r : cmd.conditions()) {
            if (r.getValue() == null || r.getValue().isBlank()) {
                continue;
            }
            var f = r.getField();
            if (JobIndexField.TITLE.equals(f) || JobIndexField.FULL_DESCRIPTION.equals(f)) {
                bag.add(r.getValue().trim());
            }
        }
        return String.join(" ", bag).trim();
    }
}
