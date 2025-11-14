/*
 * Copyright 2024 breakin Inc. - All Rights Reserved.
 * TheirStack API Crawler Schema (MySQL)
 */

-- ========================================
-- TheirStack Job 크롤링 테이블
-- ========================================
-- TheirStack API로 수집한 Job 데이터를 저장하는 테이블
-- FK 제약 조건 없음 (애플리케이션 레벨에서 관리)
-- ========================================

CREATE TABLE IF NOT EXISTS crawl_theirstack_jobs
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- TheirStack 원본 데이터
    their_stack_job_id   BIGINT NOT NULL COMMENT 'TheirStack Job ID (고유값)',
    raw_data             TEXT NOT NULL COMMENT '전체 JobData JSON (원본 보관)',

    -- 주요 컬럼 (빠른 조회/필터링용)
    company              VARCHAR(100),
    url                  TEXT,
    title                TEXT,
    location             TEXT,
    seniority            VARCHAR(50),
    date_posted          TIMESTAMP,
    description          TEXT,

    -- 처리 상태
    status               VARCHAR(20) NOT NULL COMMENT '처리 상태 (WAIT, SUCCESS, FAILED)',
    job_id               BIGINT COMMENT 'jobs 테이블 FK (Job 생성 후 업데이트)',
    error_message        TEXT,

    -- 타임스탬프
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    -- 제약조건
    CONSTRAINT uk_theirstack_job_id UNIQUE (their_stack_job_id)
) COMMENT='TheirStack API로 수집한 Job 데이터';

-- 인덱스
CREATE INDEX idx_crawl_theirstack_jobs_status ON crawl_theirstack_jobs (status);
CREATE INDEX idx_crawl_theirstack_jobs_company ON crawl_theirstack_jobs (company);
CREATE INDEX idx_crawl_theirstack_jobs_date_posted ON crawl_theirstack_jobs (date_posted DESC);
CREATE INDEX idx_crawl_theirstack_jobs_created_at ON crawl_theirstack_jobs (created_at DESC);

-- ========================================
-- 재시도 로직을 위한 retry_count 컬럼 추가
-- ========================================
-- 실패한 TheirStack Job 데이터를 재처리하기 위한 컬럼 (최대 3회)
-- ========================================

-- crawl_theirstack_jobs 테이블에 retry_count 추가
ALTER TABLE crawl_theirstack_jobs
ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0 NOT NULL COMMENT '재시도 횟수 (최대 3회)';
