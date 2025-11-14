# Auth Module

> **📝 안내:** 이 오픈소스 버전에서는 구현 코드가 제거되었습니다.

## 구현 코드를 제거한 이유

이 모듈은 Spring Security를 활용한 **쿠키 기반 세션 관리** 인증 로직을 포함하고 있습니다. 
구현은 보편적인 웹 세션에 대한 보안을 두어 안전하다고 생각되지만, 그래도 혹시 모르니까, 제가 모르는 부분에서 하자가 있을까봐 겁이나서 주요 인터페이스 정도만 공개합니다. 

이 모듈은 **private 레포지토리에서 완전히 동작하며 프로덕션에서 사용 중**입니다. 아래 문서는 유사한 기능을 구현하고자 하는 분들을 위해 아키텍처와 구현 요구사항을 설명합니다.

---

## 이 모듈이 하는 일

Spring Boot 애플리케이션을 위한 세션 기반 인증 모듈로, 다음 기능들을 제공합니다:

### 핵심 기능
- **세션 관리**: 쿠키 기반 세션 저장소, 만료 시간 설정 가능
- **OAuth 통합**: OAuth 프로바이더(Google, Kakao 등) 콜백 처리
- **유연한 보안 설정**: 경로 기반 인증 규칙 설정 가능
- **저장소 추상화**: `SessionStore` 인터페이스로 다양한 백엔드 지원 (InMemory, RDBMS with cache, Redis 준비됨)
- **자동 정리**: 스케줄러를 통한 만료된 세션 자동 삭제

### 주요 컴포넌트

#### 1. SessionStore - 세션 저장소 인터페이스

세션 영속성을 위한 추상화 인터페이스로, 다양한 백엔드 구현을 지원합니다.

```java
package dev.devrunner.auth.store;

import dev.devrunner.auth.model.SessionUser;
import java.time.Duration;
import java.util.Optional;

/**
 * 세션 저장소 인터페이스
 *
 * 구현체:
 * - InMemorySessionStore: 인메모리 저장소 (개발/단일 인스턴스용)
 * - RedisSessionStore: Redis 저장소 (프로덕션/멀티 인스턴스용) - 추후 구현
 */
public interface SessionStore {

    /**
     * 세션 생성 및 저장
     *
     * @param user 세션에 저장할 유저 정보
     * @param ttl  세션 만료 시간
     * @return 생성된 세션 ID
     */
    String createSession(SessionUser user, Duration ttl);

    /**
     * 세션 조회
     *
     * @param sessionId 세션 ID
     * @return 세션 유저 정보 (만료되었거나 존재하지 않으면 empty)
     */
    Optional<SessionUser> getSession(String sessionId);

    /**
     * 세션 삭제 (로그아웃)
     *
     * @param sessionId 세션 ID
     */
    void deleteSession(String sessionId);

    /**
     * 세션 만료 시간 연장
     *
     * @param sessionId 세션 ID
     * @param ttl       연장할 시간
     */
    void extendSession(String sessionId, Duration ttl);
}
```

**프로덕션에서 사용중인 구현체 :**
- `InMemorySessionStore`: ConcurrentHashMap 기반, 단일 인스턴스용, ( 로컬 테스트용 )
- `CacheableSessionStore`: RDBMS + 캐시 조합 (로컬 케시 , 단일 인스턴스 api 가정 )

#### 2. SessionAuthenticationFilter - 세션 인증 필터

매 요청마다 쿠키에서 세션을 검증하고 SecurityContext에 인증 정보를 저장합니다.

```java
package dev.devrunner.auth.filter;



import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * 세션 기반 인증 필터
 * <p>
 * 요청에서 SESSION_ID 쿠키를 추출하여 세션을 검증하고,
 * 유효한 경우 SecurityContext에 인증 정보를 저장합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionStore sessionStore;
    private static final String SESSION_COOKIE_NAME = "XXXX";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Cookie에서 세션 ID 추출
        var sessionId = extractSessionIdFromCookie(request);

        if (sessionId != null) {
            // 2. 세션 검증
            validateSession(sessionId).ifPresent(sessionUser -> {
                // 3. SecurityContext에 인증 정보 저장
                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                sessionUser,  // Principal
                                null,         // Credentials (비밀번호 불필요)
                                Collections.emptyList()  // Authorities (권한)
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Session authenticated: userId={}, sessionId={}",
                        sessionUser.getUserId(), sessionId);
            });
        }

        filterChain.doFilter(request, response);
    }

    private Optional<SessionUser> validateSession(String sessionId) {
        return sessionStore.getSession(sessionId);
    }

}
```

