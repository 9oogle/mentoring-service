-- ==========================================================
-- V1__init_schema.sql
-- ==========================================================

-- 멘토링 카테고리
CREATE TABLE IF NOT EXISTS p_mentoring_category (
    mentoring_category_id UUID         PRIMARY KEY,
    name                  VARCHAR(50)  NOT NULL,
    code                  VARCHAR(10)  NOT NULL,
    sort_order            INTEGER,
    active                BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL,
    deleted_at            TIMESTAMP,
    created_by            UUID         NOT NULL,
    updated_by            UUID         NOT NULL,
    deleted_by            UUID
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_mentoring_category_code_active
    ON p_mentoring_category (code)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_mentoring_category_name_active
    ON p_mentoring_category (name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_mentoring_category_sort_active
    ON p_mentoring_category (sort_order)
    WHERE active = TRUE;

-- 멘토링
CREATE TABLE IF NOT EXISTS p_mentoring (
    mentoring_id     UUID         PRIMARY KEY,
    mentor_id        UUID         NOT NULL,
    mentor_name      VARCHAR(100) NOT NULL,
    mentor_field     VARCHAR(100) NOT NULL,
    mentor_email     VARCHAR(100) NOT NULL,
    category_id      UUID,
    category_name    VARCHAR(50),
    category_code    VARCHAR(10),
    title            VARCHAR(100) NOT NULL,
    subtitle         VARCHAR(255),
    description      TEXT,
    duration         VARCHAR(50),
    status           VARCHAR(50),
    format           VARCHAR(50),
    mentoring_type   VARCHAR(50),
    session_count    INTEGER      NOT NULL DEFAULT 1,
    max_participants INTEGER      NOT NULL DEFAULT 1,
    end_date         DATE,
    exclude_holidays BOOLEAN      NOT NULL DEFAULT TRUE,
    price            BIGINT       NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    deleted_at       TIMESTAMP,
    created_by       UUID         NOT NULL,
    updated_by       UUID         NOT NULL,
    deleted_by       UUID
);

CREATE INDEX IF NOT EXISTS idx_mentoring_mentor_id ON p_mentoring (mentor_id);
CREATE INDEX IF NOT EXISTS idx_mentoring_status    ON p_mentoring (status) WHERE deleted_at IS NULL;

-- 반복 패턴 (ElementCollection)
CREATE TABLE IF NOT EXISTS p_repeat_pattern (
    mentoring_id  UUID    NOT NULL,
    pattern_order INTEGER NOT NULL,
    day_of_week   VARCHAR(20),
    start_time    TIME,
    end_time      TIME,
    PRIMARY KEY (mentoring_id, pattern_order),
    CONSTRAINT fk_repeat_pattern_mentoring
        FOREIGN KEY (mentoring_id) REFERENCES p_mentoring (mentoring_id)
);

-- 멘토링 세션 (ElementCollection)
CREATE TABLE IF NOT EXISTS p_mentoring_session (
    mentoring_id       UUID    NOT NULL,
    session_order      INTEGER NOT NULL,
    session_date       DATE,
    session_start_time TIME,
    session_end_time   TIME,
    status             VARCHAR(20),
    PRIMARY KEY (mentoring_id, session_order),
    CONSTRAINT fk_mentoring_session_mentoring
        FOREIGN KEY (mentoring_id) REFERENCES p_mentoring (mentoring_id)
);

CREATE INDEX IF NOT EXISTS idx_mentoring_session_date
    ON p_mentoring_session (mentoring_id, session_date);

-- 멘토링 예약
CREATE TABLE IF NOT EXISTS p_mentoring_booking (
    id              UUID         PRIMARY KEY,
    mentoring_id    UUID         NOT NULL,
    mentor_id       UUID         NOT NULL,
    mentor_name     VARCHAR(255),
    category_code   VARCHAR(255),
    category_name   VARCHAR(255),
    title           VARCHAR(255),
    subtitle        VARCHAR(255),
    student_id      UUID         NOT NULL,
    student_name    VARCHAR(100) NOT NULL,
    status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    request_message TEXT,
    closed_by       UUID,
    closed_at       TIMESTAMP,
    close_reason    TEXT,
    order_id        UUID,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    deleted_at      TIMESTAMP,
    created_by      UUID         NOT NULL,
    updated_by      UUID         NOT NULL,
    deleted_by      UUID
);

CREATE INDEX IF NOT EXISTS idx_booking_mentoring_id ON p_mentoring_booking (mentoring_id);
CREATE INDEX IF NOT EXISTS idx_booking_mentor_id    ON p_mentoring_booking (mentor_id)   WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_booking_student_id   ON p_mentoring_booking (student_id)  WHERE deleted_at IS NULL;

-- 예약 세션
CREATE TABLE IF NOT EXISTS p_booking_session (
    id                 UUID        PRIMARY KEY,
    booking_id         UUID        NOT NULL,
    session_date       DATE,
    session_start_time TIME,
    session_end_time   TIME,
    progress_status    VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    CONSTRAINT fk_booking_session_booking
        FOREIGN KEY (booking_id) REFERENCES p_mentoring_booking (id)
);

CREATE INDEX IF NOT EXISTS idx_booking_session_booking_id ON p_booking_session (booking_id);