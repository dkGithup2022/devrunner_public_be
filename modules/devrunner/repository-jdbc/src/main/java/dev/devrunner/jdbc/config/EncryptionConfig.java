package dev.devrunner.jdbc.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Encryption 모듈을 스캔하여 EmailEncryptor 빈을 등록
 * - UserJdbcRepository 등에서 EmailEncryptor 사용
 */
@Configuration
@ComponentScan(basePackages = "dev.devrunner.encryption")
public class EncryptionConfig {
}
