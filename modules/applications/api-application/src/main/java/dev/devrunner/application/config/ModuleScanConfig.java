package dev.devrunner.application.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * Module Scan Configuration
 * <p>
 * 멀티모듈 프로젝트의 컴포넌트 스캔 범위를 중앙에서 관리합니다.
 *
 * @ComponentScan: 일반 컴포넌트(@Component, @Service 등) 스캔
 * @EnableJdbcRepositories: Spring Data JDBC Repository 인터페이스 스캔 및 구현체 자동 생성
 */
@Configuration
@ComponentScan(basePackages = {
        "dev.devrunner.api",
        "dev.devrunner.search",
        "dev.devrunner.service",
        "dev.devrunner.infra",
        "dev.devrunner.jdbc", // JDBC 구현체 클래스 스캔
        "dev.devrunner.auth",
        "dev.devrunner.outbox",
        "dev.devrunner.elasticsearch",
        "dev.devrunner.encryption"
       // "dev.devrunner.logging" // LoggingFilter 스캔
})
@EnableJdbcRepositories(basePackages = {
        "dev.devrunner.jdbc",
        "dev.devrunner.outbox",
        "dev.devrunner.auth"  // SessionEntityRepository 스캔
})
public class ModuleScanConfig {
}
