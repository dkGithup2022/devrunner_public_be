package dev.devrunner.application.batch.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@Configuration
@ComponentScan(basePackages = {
        "dev.devrunner.infra",
        "dev.devrunner.jdbc", // JDBC 구현체 클래스 스캔
        "dev.devrunner.outbox",
        "dev.devrunner.elasticsearch",
        "dev.devrunner.sync",
        "dev.devrunner.crawler"
})
@EnableJdbcRepositories(basePackages = {"dev.devrunner.jdbc", "dev.devrunner.crawler", "dev.devrunner.outbox"})
public class ScanConfig {
}
