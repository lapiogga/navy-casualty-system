-- V6: 주민번호 SHA-256 해시 컬럼 추가 (DEAD-07 중복 방지)

ALTER TABLE TB_DEAD ADD COLUMN ssn_hash VARCHAR(64);

-- 논리 삭제된 레코드는 제외하는 partial unique index
CREATE UNIQUE INDEX idx_dead_ssn_hash ON TB_DEAD (ssn_hash) WHERE deleted_at IS NULL;
