package dev.devrunner.jdbc.config;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * JDBC Module의 기본 컴포넌트 스캔 설정
 * <p>
 * 이 설정은 devrunner.module.jdbc.auto-scan=true일 때만 활성화됩니다.
 * Application 레벨에서 중앙 집중식 스캔을 사용하는 경우 (ModuleScanConfig),
 * 이 속성을 설정하지 않거나 false로 설정합니다.
 *
 * 기본값은 false (설정이 없으면 비활성화, application에서 중앙 관리)
 */
@Configuration
@ConditionalOnProperty(
    prefix = "devrunner.module.jdbc",
    name = "auto-scan",
    havingValue = "true",
    matchIfMissing = false  // 설정이 없으면 false (기본 비활성화)
)
@ComponentScan(basePackages = {
        "dev.devrunner.infra",
        "dev.devrunner.jdbc", // JDBC 구현체 클래스 스캔
        "dev.devrunner.encryption"
        // "dev.devrunner.logging" // LoggingFilter 스캔
})
@EnableJdbcRepositories(basePackages = {
        "dev.devrunner.jdbc",
})
public class JdbcComponentScan {
}
