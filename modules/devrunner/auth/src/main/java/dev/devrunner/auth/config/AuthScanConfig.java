package dev.devrunner.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * Auth Module의 기본 컴포넌트 스캔 설정
 * <p>
 * 이 설정은 devrunner.module.auth.auto-scan=true일 때만 활성화됩니다.
 * Application 레벨에서 중앙 집중식 스캔을 사용하는 경우,
 * 이 속성을 설정하지 않거나 false로 설정합니다.
 *
 * 기본값은 false (설정이 없으면 비활성화, application에서 중앙 관리)
 */
@Configuration
@ConditionalOnProperty(
    prefix = "devrunner.module.auth",
    name = "auto-scan",
    havingValue = "true",
    matchIfMissing = false
)
@ComponentScan(basePackages = {
    "dev.devrunner.auth"
})
@EnableJdbcRepositories(basePackages = {
        "dev.devrunner.auth",
})
public class AuthScanConfig
{
}
