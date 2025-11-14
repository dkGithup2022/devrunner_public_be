package dev.devrunner.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LogContextTest {

    private static final Logger log = LoggerFactory.getLogger(LogContextTest.class);

    @AfterEach
    void cleanup() {
        LogContext.clear();
    }

    @Test
    void testApiContext() {
        // Given
        String endpoint = "/api/jobs/search";
        String method = "POST";

        // When
        LogContext.setApiContext(endpoint, method);

        // Then - Check logs in console
        log.info("Testing API context logging");
        log.info("Search results found: 10 items");
        log.warn("Some warning message");

        LogContext.clearApiContext();
    }

    @Test
    void testBatchContext() {
        // Given
        String jobName = "ResourceCrawlTask";

        // When
        LogContext.setBatchContext(jobName);

        // Then - Check logs in console
        log.info("Testing batch context logging");
        log.info("Crawling started for Google jobs");
        log.info("Crawling completed: 50 jobs processed");

        LogContext.clearBatchContext();
    }

    @Test
    void testMixedContext() {
        // API context
        LogContext.setApiContext("/api/jobs/1", "GET");
        log.info("Fetching job details");

        // Clear and set batch context
        LogContext.clearApiContext();
        LogContext.setBatchContext("EsSyncTask");
        log.info("Indexing job to Elasticsearch");

        LogContext.clearBatchContext();
    }

    @Test
    void testNoContext() {
        // No MDC context set
        log.info("Log without any context");
    }
}
