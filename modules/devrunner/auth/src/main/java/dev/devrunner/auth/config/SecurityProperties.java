package dev.devrunner.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Security 설정 프로퍼티
 *
 * application.yml에서 인증 규칙을 설정할 수 있습니다.
 */
@Component
@ConfigurationProperties(prefix = "devrunner.security")
@Getter
@Setter
public class SecurityProperties {

    /**
     * 인증이 필요한 경로 패턴 (기본값: /api/**)
     */
    private String securedPathPattern = "/api/**";

    /**
     * 인증 없이 접근 가능한 경로 목록
     */
    private List<String> permitAllPaths = new ArrayList<>(List.of(
            "/api/auth/login",
            "/api/auth/signup"
    ));

    /**
     * 전체 경로에 인증 적용 여부 (false면 securedPathPattern만 적용)
     */
    private boolean secureAllPaths = false;
}
