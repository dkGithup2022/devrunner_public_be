/*
 * Copyright 2024 breakin Inc. - All Rights Reserved.
 * Session Management Schema
 */

-- ========================================
-- 세션 관리 테이블
-- ========================================
-- 사용자 로그인 세션을 저장하는 테이블
-- ※ 추후 Redis로 마이그레이션 예정
-- ========================================

-- 로그인 세션 테이블
CREATE TABLE IF NOT EXISTS login_sessions (
    -- 세션 ID (UUID)
    session_key VARCHAR(36) PRIMARY KEY,

    -- 사용자 ID (users.id 참조, FK 없음)
    user_id BIGINT NOT NULL,

    -- 세션 생성 시간
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    -- 세션 만료 시간 (기본: 생성 후 3일)
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- ========================================
-- 인덱스
-- ========================================

-- 만료된 세션 조회 및 정리용 인덱스
-- 스케줄러가 만료된 세션을 삭제할 때 사용
CREATE INDEX IF NOT EXISTS idx_login_sessions_expires_at
ON login_sessions(expires_at);

-- 사용자별 세션 조회용 인덱스
-- 다중 기기 로그인 관리 또는 사용자 세션 조회 시 사용
CREATE INDEX IF NOT EXISTS idx_login_sessions_user_id
ON login_sessions(user_id);

-- ========================================
-- 설계 노트
-- ========================================
-- 1. 세션 만료 정책
--    - 기본 만료 기간: 3일 (72시간)
--    - 연장 정책: 명시적 호출 없이는 연장하지 않음 (Absolute expiration)
--
-- 2. 만료 세션 정리
--    - 실시간 정리: getSession() 호출 시 만료된 세션은 즉시 삭제
--    - 배치 정리: 스케줄러가 매일 새벽 3시에 만료된 세션 일괄 삭제
--
-- 3. 다중 기기 로그인
--    - 현재: 허용 (user_id당 여러 session_key 존재 가능)
--    - 제한 필요 시: user_id에 UNIQUE 제약 추가
--
-- 4. 보안 고려사항
--    - session_key는 UUID v4 사용 (예측 불가능)
--    - 전송 시 HttpOnly, Secure 쿠키 사용
--    - HTTPS 필수
--
-- 5. 성능 최적화
--    - CacheableSessionStore: 2-tier 캐싱 (메모리 + DB)
--    - 조회 빈도가 높은 세션은 메모리 캐시 hit
--    - DB는 영구 저장소 및 재시작 시 복구용
--
-- 6. 마이그레이션 계획
--    - 현재: RDBMS (H2/PostgreSQL)
--    - 목표: Redis (TTL 자동 관리, 성능 향상)
--    - 인터페이스: SessionStore (구현체만 교체)
--
