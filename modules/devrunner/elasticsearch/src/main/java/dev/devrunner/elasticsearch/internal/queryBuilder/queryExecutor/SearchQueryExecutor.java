package dev.devrunner.elasticsearch.internal.queryBuilder.queryExecutor;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import dev.devrunner.elasticsearch.exception.ElasticsearchQueryException;
import dev.devrunner.elasticsearch.internal.queryBuilder.SortOption;
import jakarta.json.stream.JsonGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchQueryExecutor {
    private final ElasticsearchClient esClient;

    public <T> SearchResult<T> search(String indexName, Query query, int from, int size, Class<T> resultType) {
        try {
            SearchRequest req = new SearchRequest.Builder()
                    .index(indexName)
                    .query(query)
                    .from(from)
                    .size(size)
                    .build();

            // 실제 직렬화된 JSON 로깅 (toString() 아님!)
            StringWriter w = new StringWriter();
            JsonGenerator g = esClient._transport().jsonpMapper().jsonProvider().createGenerator(w);
            esClient._transport().jsonpMapper().serialize(req, g);
            g.close();
            log.info("실제 전송되는 Elasticsearch 쿼리(search): {}", w.toString());


            var response = esClient.search(req, resultType);
            long totalHits = response.hits().total().value();

            List<T> docs = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();

            return new SearchResult<>(docs, totalHits);

        } catch (Exception e) {
            throw new ElasticsearchQueryException(indexName, e);
        }
    }

    public <T> SearchResult<T> searchWithSort(String indexName, Query query, int from, int size, SortOption sortOption, Class<T> resultType) {
        log.info(query.toString());
        try {
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index(indexName)
                    .query(query)
                    .from(from)
                    .sort(s -> s
                            .field(f -> f
                                    .field(sortOption.field())
                                    .order(sortOption.order())
                                    .missing("_last")
                            )
                    )
                    .size(size)
                    .build();

            // JSON으로 실제 쿼리 출력
            StringWriter writer = new StringWriter();
            JsonGenerator generator = esClient._transport().jsonpMapper().jsonProvider().createGenerator(writer);
            esClient._transport().jsonpMapper().serialize(searchRequest, generator);
            generator.close();

            log.info("실제 전송되는 Elasticsearch 쿼리: {}", writer.toString());

            var response = esClient.search(searchRequest, resultType);

            long totalHits = response.hits().total().value();

            List<T> docs = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();

            return new SearchResult<>(docs, totalHits);

        } catch (Exception e) {
            log.error("Elasticsearch search 실패 - Index: {}, Error: {}", indexName, e.getMessage(), e);
            throw new ElasticsearchQueryException(indexName, e);
        }
    }

    public <T> List<T> searchByVector(String indexName, List<Float> queryVector, Query filterQuery, int size, Class<T> resultType) {
        try {
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index(indexName)
                    .knn(knn -> knn
                            .queryVector(queryVector)
                            .field("vector")
                            .k(100)
                            .numCandidates(300)
                            .filter(filterQuery)
                    )
                    .size(size)
                    .build();

            // 실제 쿼리 JSON 출력
            StringWriter writer = new StringWriter();
            JsonGenerator generator = esClient._transport().jsonpMapper().jsonProvider().createGenerator(writer);
            esClient._transport().jsonpMapper().serialize(searchRequest, generator);
            generator.close();
            log.info("실제 전송되는 KNN 쿼리: {}", writer.toString());

            var response = esClient.search(searchRequest, resultType);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (Exception e) {
            log.error("Elasticsearch KNN search 실패 - Index: {}, Error: {}", indexName, e.getMessage(), e);
            throw new ElasticsearchQueryException(indexName, e);
        }
    }

    public <T> T getById(String indexName, String docId, Class<T> resultType) {
        try {
            GetRequest getRequest = new GetRequest.Builder()
                    .index(indexName)
                    .id(docId)
                    .build();

            var response = esClient.get(getRequest, resultType);

            if (!response.found()) {
                throw new ElasticsearchQueryException(indexName,
                    new IllegalArgumentException("Document not found: " + docId));
            }

            return response.source();

        } catch (Exception e) {
            log.error("Elasticsearch get 실패 - Index: {}, DocId: {}, Error: {}", indexName, docId, e.getMessage(), e);
            throw new ElasticsearchQueryException(indexName, e);
        }
    }

}
