package dev.devrunner.service.integrationConfig;

import dev.devrunner.auth.config.AuthScanConfig;
import dev.devrunner.outbox.config.OutboxScanConfig;
import dev.devrunner.jdbc.config.JdbcComponentScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 서비스 레이어 통합 테스트용 설정
 * <p>
 * - JDBC 레포지토리 스캔 (JdbcComponentScan)
 * - 서비스 레이어 스캔
 * - 인증 관련 컴포넌트 스캔
 */
@Import({JdbcComponentScan.class, AuthScanConfig.class, OutboxScanConfig.class})
@ComponentScan(basePackages = {
        "dev.devrunner.service",
})
public class IntegrationConfig {
}
