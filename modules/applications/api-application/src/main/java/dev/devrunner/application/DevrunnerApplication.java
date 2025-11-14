package dev.devrunner.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * Multi-Module Spring Boot Application
 *
 * 컴포넌트 스캔 설정은 application.config.ModuleScanConfig에서 중앙 관리됩니다.
 */
@SpringBootApplication(
    scanBasePackages = "dev.devrunner.application.config"
)
@EnableScheduling
public class DevrunnerApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SpringApplication.run(DevrunnerApplication.class, args);
    }
}
