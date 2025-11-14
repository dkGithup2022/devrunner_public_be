package dev.devrunner.elasticsearch.api;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.devrunner.elasticsearch.document.DocBase;
import dev.devrunner.elasticsearch.exception.ElasticsearchQueryException;
import dev.devrunner.elasticsearch.internal.indexer.DocIndexer;
import dev.devrunner.model.common.Popularity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractDocPopularityManager<T extends DocBase> implements DocPopularityManager {

    private final ElasticsearchClient esClient;
    private final ObjectMapper objectMapper;

    public AbstractDocPopularityManager(
            ElasticsearchClient esClient,
            ObjectMapper esObjectMapper
    ) {
        this.esClient = esClient;
        this.objectMapper = esObjectMapper;

        log.info("[AbstractDocPopularityManager] Injected ElasticsearchClient hash: {}", System.identityHashCode(esClient));
        log.info("[AbstractDocPopularityManager] Injected esObjectMapper hash: {}", System.identityHashCode(esObjectMapper));
    }

    protected abstract String index();

    protected abstract Class<T> docType();

    protected abstract T updatePopularity(T doc, Popularity popularity);

    protected abstract DocIndexer docIndexer();

    public void updatePopularity(String docId, Popularity popularity) {
        var document = findById(docId);
        if (document == null) {
            log.info("no document with index:{} , id {}", index(), docId);
            return;
        }

        var updated = updatePopularity(document, popularity);

        DocIndexer indexer = docIndexer();
        indexer.indexOne(updated);
    }

    protected T findById(String docId) {
        try {
            // 1. JsonData로 응답 받기
            GetResponse<JsonData> response = esClient.get(
                    g -> g.index(index()).id(docId),
                    JsonData.class
            );

            if (!response.found()) {
                log.info("document not found in index: {} with id: {}", index(), docId);
                return null;
            }

            JsonData sourceData = response.source();
            T mapped = objectMapper.readValue(sourceData.to(JsonNode.class).toString(), docType());

            log.debug("mapped to DTO: {}", mapped);

            return mapped;

        } catch (Exception e) {
            log.error("Error fetching or mapping document: {}", docId, e);
            throw new ElasticsearchQueryException("Error fetching or mapping document - id : " + docId);
        }
    }

}
