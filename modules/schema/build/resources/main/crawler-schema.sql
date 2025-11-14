/*
 * Copyright 2024 breakin Inc. - All Rights Reserved.
 * Crawler Step Tables Schema
 */

-- ========================================
-- Crawler Step 테이블들
-- ========================================
-- 외부 리소스 크롤링 배치의 각 단계별 데이터를 저장하는 테이블들
-- FK 제약 조건 없음 (애플리케이션 레벨에서 관리)
-- ========================================

-- Step 1: URL 수집 단계
CREATE TABLE IF NOT EXISTS crawl_job_urls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company VARCHAR(100) NOT NULL,           -- GOOGLE, NETFLIX, META
    url VARCHAR(1000) NOT NULL UNIQUE,
    title VARCHAR(500),                      -- Job title extracted from listing
    status VARCHAR(50) NOT NULL,             -- WAIT, SUCCESS, FAILED
    retry_count INT DEFAULT 0 NOT NULL,      -- 재시도 횟수 (최대 3회)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE
);

-- Step 2: 콘텐츠 크롤링 및 요약 단계
CREATE TABLE IF NOT EXISTS crawl_job_contents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url_id BIGINT NOT NULL,                  -- crawl_job_urls 참조 (FK 없음)
    markdown_content TEXT,                   -- Firecrawl로 추출한 Markdown
    shortened_content TEXT,                  -- GPT로 요약한 내용
    status VARCHAR(50) NOT NULL,             -- WAIT, SUCCESS, FAILED
    job_id BIGINT,                           -- jobs 테이블 참조 (FK 없음)
    error_message TEXT,
    retry_count INT DEFAULT 0 NOT NULL,      -- 재시도 횟수 (최대 3회)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE
);
