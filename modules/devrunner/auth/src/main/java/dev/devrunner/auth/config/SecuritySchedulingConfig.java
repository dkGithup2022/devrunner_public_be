package dev.devrunner.auth.config;


import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling  // 임시: 세션 정리 스케줄러 활성화 (추후 Redis 전환 시 제거 예정)
public class SecuritySchedulingConfig {
}
