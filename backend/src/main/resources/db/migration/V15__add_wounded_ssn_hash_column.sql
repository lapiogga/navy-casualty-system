-- V15: 상이자 주민번호 SHA-256 해시 컬럼 추가 (WOND 중복 방지)

ALTER TABLE TB_WOUNDED ADD COLUMN ssn_hash VARCHAR(64);

-- 논리 삭제된 레코드는 제외하는 partial unique index
CREATE UNIQUE INDEX idx_wounded_ssn_hash ON TB_WOUNDED (ssn_hash) WHERE deleted_at IS NULL;
