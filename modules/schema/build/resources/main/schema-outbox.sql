/*
 * Copyright 2024 breakin Inc. - All Rights Reserved.
 * Outbox Events Schema
 */

-- ========================================
-- Outbox Events 테이블
-- ========================================
-- 변경 이벤트를 추적하여 외부 시스템(ES 등)과 동기화
CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type VARCHAR(50) NOT NULL,      -- JOB, TECH_BLOG, COMMUNITY_POST
    target_id BIGINT NOT NULL,
    update_type VARCHAR(50) NOT NULL,      -- CREATED, UPDATED, POPULARITY_ONLY, DELETED
    status VARCHAR(20) NOT NULL,           -- WAIT, PROCESSING, COMPLETED, FAILED
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE
);

-- Index for efficient polling of pending events
CREATE INDEX IF NOT EXISTS idx_outbox_status_updated ON outbox_events(status, updated_at);

-- Index for looking up events by target
CREATE INDEX IF NOT EXISTS idx_outbox_target ON outbox_events(target_type, target_id);

-- Index for filtering by update type
CREATE INDEX IF NOT EXISTS idx_outbox_update_type ON outbox_events(update_type, status);
