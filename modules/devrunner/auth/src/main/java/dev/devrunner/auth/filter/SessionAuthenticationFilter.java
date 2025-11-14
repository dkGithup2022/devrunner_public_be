package dev.devrunner.auth.filter;

import dev.devrunner.auth.model.SessionUser;

import dev.devrunner.auth.store.SessionStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
    private static final String SESSION_COOKIE_NAME = "SESSION_ID";

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

    /**
     * Cookie에서 세션 ID 추출
     */
    private String extractSessionIdFromCookie(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (var cookie : cookies) {
            if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
