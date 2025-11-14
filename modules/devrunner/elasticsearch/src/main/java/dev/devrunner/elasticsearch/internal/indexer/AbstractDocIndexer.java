package dev.devrunner.elasticsearch.internal.indexer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import dev.devrunner.elasticsearch.document.DocBase;
import dev.devrunner.elasticsearch.exception.DocumentIndexingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Component
public abstract class AbstractDocIndexer implements DocIndexer {

    protected final ElasticsearchClient esClient;

    /**
     * name of index that repository access
     * only purpose to decide what index you can access in this::indexOne() and this::indexAll
     * <p>
     * return : {name of index}, eg: develop_translation_article, develop_question, prod_question ....
     */
    protected abstract String getIndex();

    /**
     * id of document that document setted
     * only purpose to decide what id you save in this::indexOne() and this::indexAll
     * <p>
     * return : {id of doc}, eg: TRANSLATION_1,  QUESTION_101
     */
    protected abstract String getDocId(DocBase doc);

    protected abstract boolean validateDoc(DocBase doc);

    /**
     * index one document
     */
    public IndexResponseType indexOne(DocBase doc) {
        if (!validateDoc(doc))
            throw new IllegalStateException("document not validated status");

        try {
            var indexResponse = sendRequest(doc);
            return IndexResponseType.from(indexResponse.result());
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            log.error("", e);
            throw new DocumentIndexingException(doc.getDocId(), e);
        }
    }

    private IndexResponse sendRequest(DocBase doc) throws IOException {
        return esClient.index(i -> {
            var request = i
                    .index(getIndex())
                    .id(getDocId(doc))
                    .document(doc);
            return request;
        });
    }
}
