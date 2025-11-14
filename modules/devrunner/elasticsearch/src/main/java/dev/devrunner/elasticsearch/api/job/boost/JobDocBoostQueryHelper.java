package dev.devrunner.elasticsearch.api.job.boost;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import dev.devrunner.elasticsearch.document.JobDocKeywordBoost;
import dev.devrunner.elasticsearch.internal.queryBuilder.SearchCommand;
import dev.devrunner.elasticsearch.document.fieldSpec.job.JobIndexField;

import java.util.List;

public interface JobDocBoostQueryHelper {

    /**
     * TITLE / FULL_DESCRIPTION 에 검색어가 들어와 부스트 시도 조건이 되는지
     */
    boolean hasSearchWord(SearchCommand<JobIndexField> command);

    /**
     * percolate 로 부스트 키워드(정규화된 canonical, boost, priority 등) 추출
     */
    List<JobDocKeywordBoost> extractBoostKeywords(SearchCommand<JobIndexField> command);

    /**
     * 부스트 키워드를 ES should 쿼리(phrase) 리스트로 생성
     */
    List<Query> buildBoostQueries(SearchCommand<JobIndexField> command);
}
