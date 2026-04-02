-- V8: 사용자/역할 테이블 생성 (AUTH-01~07)

CREATE TABLE TB_USER (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'VIEWER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_count INT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    last_modified_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    delete_reason VARCHAR(500)
);

-- role: ADMIN, MANAGER, OPERATOR, VIEWER
-- 기본 관리자 계정은 Phase 2에서 생성

CREATE INDEX idx_user_username ON TB_USER(username);
CREATE INDEX idx_user_role ON TB_USER(role);
