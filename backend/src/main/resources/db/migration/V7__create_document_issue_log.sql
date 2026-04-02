-- V7: 문서 발급 이력 테이블 생성 (DOCU-08)

CREATE TABLE TB_DOCUMENT_ISSUE (
    id BIGSERIAL PRIMARY KEY,
    document_type VARCHAR(50) NOT NULL,
    target_table VARCHAR(50),
    target_id BIGINT,
    issue_purpose VARCHAR(500) NOT NULL,
    issued_by VARCHAR(50) NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_document_issue_issued_at ON TB_DOCUMENT_ISSUE(issued_at);
