-- V16: TB_REVIEW ssn_hash + rank_id + branch_id + status 컬럼 추가
-- ssn_hash: 주민번호 SHA-256 해시 (중복 검증용, Dead/Wounded와 동일 패턴)
-- rank_id/branch_id: REVW-01 복합 검색 요구사항 충족용 (V5에서 누락)
-- status: 4단계 상태 전이 (REGISTERED -> UNDER_REVIEW -> CONFIRMED -> NOTIFIED)

ALTER TABLE TB_REVIEW ADD COLUMN ssn_hash VARCHAR(64);
ALTER TABLE TB_REVIEW ADD COLUMN rank_id BIGINT REFERENCES TB_RANK_CODE(id);
ALTER TABLE TB_REVIEW ADD COLUMN branch_id BIGINT REFERENCES TB_BRANCH_CODE(id);
ALTER TABLE TB_REVIEW ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED';

CREATE UNIQUE INDEX idx_review_ssn_hash ON TB_REVIEW (ssn_hash) WHERE deleted_at IS NULL;
