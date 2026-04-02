-- V6: 감사 로그 테이블 생성 (AUDIT-01~03)
-- append-only: 수정/삭제 불가 (감사 컬럼 없음 - 이력 변경 방지)

CREATE TABLE TB_AUDIT_LOG (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    target_table VARCHAR(50),
    target_id BIGINT,
    detail TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- action: VIEW, CREATE, UPDATE, DELETE, PRINT, EXPORT

CREATE INDEX idx_audit_log_created_at ON TB_AUDIT_LOG(created_at);
CREATE INDEX idx_audit_log_user_id ON TB_AUDIT_LOG(user_id);
