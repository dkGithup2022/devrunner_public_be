/*
 * Copyright 2024 breakin Inc. - All Rights Reserved.
 * MySQL Schema
 */

-- ========================================
-- 테이블명 및 컬럼명 규칙
-- ========================================
-- 테이블명: 소문자 + 복수형 (예: users, orders, products)
-- 컬럼명: 스네이크케이스 소문자 (예: user_id, created_at)
-- 이유:
--   - Spring Data JDBC 기본 네이밍 전략과 일치
--   - PostgreSQL, MySQL 등 프로덕션 DB 마이그레이션 용이
--   - 대소문자 혼용으로 인한 매핑 이슈 방지
-- ========================================

-- Examples 테이블 생성
CREATE TABLE IF NOT EXISTS examples (
    example_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Users 테이블 생성
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    google_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(512) NOT NULL UNIQUE,  -- 암호화된 이메일 저장 (Base64 인코딩)
    nickname VARCHAR(100) NOT NULL UNIQUE,
    user_role VARCHAR(50) NOT NULL DEFAULT 'USER',

    -- NotificationSettings (Embedded) - 컬럼으로 flatten
    email_enabled BOOLEAN DEFAULT TRUE,
    new_job_alerts BOOLEAN DEFAULT TRUE,
    reply_alerts BOOLEAN DEFAULT TRUE,
    like_alerts BOOLEAN DEFAULT TRUE,

    -- 활동 통계
    post_count BIGINT DEFAULT 0,
    comment_count BIGINT DEFAULT 0,
    likes_received BIGINT DEFAULT 0,
    like_given_count BIGINT DEFAULT 0,
    bookmark_count BIGINT DEFAULT 0,

    -- 메타 정보
    last_login_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_withdrawn BOOLEAN NOT NULL DEFAULT FALSE,
    withdrawn_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- User 관심 회사 테이블 (1:N)
CREATE TABLE IF NOT EXISTS user_interested_companies (
    user_id BIGINT NOT NULL,
    company_name VARCHAR(255) NOT NULL
);
CREATE INDEX idx_user_interested_companies_user_id ON user_interested_companies(user_id);

-- User 관심 지역 테이블 (1:N)
CREATE TABLE IF NOT EXISTS user_interested_locations (
    user_id BIGINT NOT NULL,
    location_name VARCHAR(255) NOT NULL
);
CREATE INDEX idx_user_interested_locations_user_id ON user_interested_locations(user_id);

-- TechBlogs 테이블 생성
CREATE TABLE IF NOT EXISTS tech_blogs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(512) NOT NULL UNIQUE,
    company VARCHAR(255),
    title VARCHAR(500) NOT NULL,
    one_liner VARCHAR(500),
    summary TEXT,
    markdown_body TEXT,
    thumbnail_url VARCHAR(512),
    original_url VARCHAR(512),

    -- Popularity (Embedded) - 컬럼으로 flatten
    view_count BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    dislike_count BIGINT NOT NULL DEFAULT 0,

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- TechBlog 기술 카테고리 테이블 (1:N)
CREATE TABLE IF NOT EXISTS tech_blog_tech_categories (
    tech_blog_id BIGINT NOT NULL,
    category_name VARCHAR(100) NOT NULL
);
CREATE INDEX idx_tech_blog_tech_categories_tech_blog_id ON tech_blog_tech_categories(tech_blog_id);

-- ========================================
-- Migration: summary_ko 컬럼 추가
-- ========================================
-- 2025-11-06: TechBlog 한국어 summary 필드 추가
-- 이미 운영 중인 테이블이므로 ALTER TABLE로 컬럼 추가
--
-- 사용 방법:
-- 1. 프로덕션 DB에서 실행 시, 먼저 백업 필수
-- 2. 아래 ALTER TABLE 문을 실행
-- 3. 기존 데이터의 summary_ko는 NULL 상태 (추후 파이프라인에서 생성)
--
-- ALTER TABLE tech_blogs ADD COLUMN summary_ko TEXT AFTER summary;

-- Jobs 테이블 생성
CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(512) NOT NULL UNIQUE,
    company VARCHAR(255) NOT NULL,
    title VARCHAR(500) NOT NULL,
    organization VARCHAR(255),
    one_line_summary VARCHAR(500),
    summary TEXT,
    min_years INT,
    max_years INT,
    experience_required BOOLEAN DEFAULT FALSE,
    career_level VARCHAR(50) DEFAULT 'ENTRY',
    employment_type VARCHAR(50) DEFAULT 'FULL_TIME',
    position_category VARCHAR(100),
    remote_policy VARCHAR(50) DEFAULT 'ONSITE',
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    is_open_ended BOOLEAN DEFAULT FALSE,
    is_closed BOOLEAN DEFAULT FALSE,
    position_introduction TEXT,
    full_description TEXT,
    has_assignment BOOLEAN DEFAULT FALSE,
    has_coding_test BOOLEAN DEFAULT FALSE,
    has_live_coding BOOLEAN DEFAULT FALSE,
    interview_count INT,
    interview_days INT,

    -- JobCompensation (Embedded) - 컬럼으로 flatten
    min_base_pay DECIMAL(15, 2),
    max_base_pay DECIMAL(15, 2),
    currency VARCHAR(10),
    unit VARCHAR(20),
    has_stock_option BOOLEAN,
    salary_note VARCHAR(1000),

    -- Popularity (Embedded) - 컬럼으로 flatten
    view_count BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    dislike_count BIGINT NOT NULL DEFAULT 0,

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Job 기술 카테고리 테이블 (1:N)
CREATE TABLE IF NOT EXISTS job_tech_categories (
    job_id BIGINT NOT NULL,
    category_name VARCHAR(100) NOT NULL
);
CREATE INDEX idx_job_tech_categories_job_id ON job_tech_categories(job_id);

-- Job 위치 테이블 (1:N)
CREATE TABLE IF NOT EXISTS job_locations (
    job_id BIGINT NOT NULL,
    location_name VARCHAR(255) NOT NULL
);
CREATE INDEX idx_job_locations_job_id ON job_locations(job_id);

-- Job 책임사항 테이블 (1:N)
CREATE TABLE IF NOT EXISTS job_responsibilities (
    job_id BIGINT NOT NULL,
    responsibility VARCHAR(500) NOT NULL
);
CREATE INDEX idx_job_responsibilities_job_id ON job_responsibilities(job_id);

-- Job 자격요건 테이블 (1:N)
CREATE TABLE IF NOT EXISTS job_qualifications (
    job_id BIGINT NOT NULL,
    qualification VARCHAR(500) NOT NULL
);
CREATE INDEX idx_job_qualifications_job_id ON job_qualifications(job_id);

-- Job 우대사항 테이블 (1:N)
CREATE TABLE IF NOT EXISTS job_preferred_qualifications (
    job_id BIGINT NOT NULL,
    preferred_qualification VARCHAR(500) NOT NULL
);
CREATE INDEX idx_job_preferred_qualifications_job_id ON job_preferred_qualifications(job_id);

-- Job 마감 체크 이력 테이블
CREATE TABLE IF NOT EXISTS job_closed_checks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    url VARCHAR(2048) NOT NULL,
    closed BOOLEAN NOT NULL,
    closed_reason VARCHAR(300),
    explanation TEXT,
    checked_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_job_closed_checks_job_id ON job_closed_checks(job_id);

-- CommunityPosts 테이블 생성
CREATE TABLE IF NOT EXISTS community_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(100) NOT NULL,
    title VARCHAR(500) NOT NULL,
    markdown_body TEXT NOT NULL,
    company VARCHAR(255),
    location VARCHAR(255),
    job_id BIGINT,
    comment_id BIGINT,

    -- Popularity (Embedded) - 컬럼으로 flatten
    view_count BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    dislike_count BIGINT NOT NULL DEFAULT 0,

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Comments 테이블 생성
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    parent_id BIGINT,

    -- CommentOrder (Embedded) - 계층 구조 관리
    comment_order INT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 0,
    sort_number INT NOT NULL DEFAULT 0,
    child_count INT NOT NULL DEFAULT 0,

    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX idx_comments_target ON comments(target_type, target_id);
CREATE INDEX idx_comments_parent ON comments(parent_id);
CREATE INDEX idx_comments_order_sort ON comments(target_type, target_id, comment_order, sort_number);

-- Reactions 테이블 생성 (좋아요/싫어요)
CREATE TABLE IF NOT EXISTS reactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    reaction_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- 중복 반응 방지를 위한 유니크 인덱스
-- CREATE UNIQUE INDEX IF NOT EXISTS uk_reactions_user_target ON reactions(user_id, target_type, target_id);
CREATE INDEX idx_reactions_target ON reactions(target_type, target_id);
CREATE INDEX idx_reactions_reaction_type ON reactions(target_type, target_id, reaction_type);

-- Bookmarks 테이블 생성
CREATE TABLE IF NOT EXISTS bookmarks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX idx_bookmarks_user ON bookmarks(user_id);
CREATE INDEX idx_bookmarks_target ON bookmarks(target_type, target_id);

-- ========================================
-- 외래키 정책
-- ========================================
-- 외래키 제약 조건(FOREIGN KEY) 사용 안함
-- 다른 테이블 참조가 필요한 경우:
--   - 제약 조건 없이 ID 컬럼만 추가 (예: USER_ID BIGINT)
--   - 참조 무결성은 애플리케이션 레벨에서 관리
--   - 장점: 유연한 데이터 관리, 순환 참조 방지, 테스트 용이성
--
-- 예시:
-- ❌ 외래키 제약 사용 (사용하지 않음)
-- CREATE TABLE ORDERS (
--     ORDER_ID BIGINT AUTO_INCREMENT PRIMARY KEY,
--     USER_ID BIGINT NOT NULL,
--     FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID)  -- 사용 안함
-- );
--
-- ✅ ID만 포함 (권장)
-- CREATE TABLE ORDERS (
--     ORDER_ID BIGINT AUTO_INCREMENT PRIMARY KEY,
--     USER_ID BIGINT NOT NULL,  -- 제약 조건 없이 ID만 저장
--     CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
-- );
