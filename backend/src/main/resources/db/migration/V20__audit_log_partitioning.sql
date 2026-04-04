-- V20: 감사 로그 테이블 연도별 파티셔닝 (AUDIT-03)
-- 기존 TB_AUDIT_LOG를 파티션 테이블로 전환한다.

-- 1. 기존 테이블 이름 변경
ALTER TABLE tb_audit_log RENAME TO tb_audit_log_old;

-- 2. 기존 인덱스 삭제 (파티션 테이블 재생성 시 충돌 방지)
DROP INDEX IF EXISTS idx_audit_log_created_at;
DROP INDEX IF EXISTS idx_audit_log_user_id;

-- 3. 동일 구조의 파티션 테이블 생성
CREATE TABLE tb_audit_log (
    id BIGSERIAL,
    user_id VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    target_table VARCHAR(50),
    target_id BIGINT,
    detail TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- 4. 연도별 파티션 생성 (2026~2031 -- 6년분)
CREATE TABLE tb_audit_log_2026 PARTITION OF tb_audit_log
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
CREATE TABLE tb_audit_log_2027 PARTITION OF tb_audit_log
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');
CREATE TABLE tb_audit_log_2028 PARTITION OF tb_audit_log
    FOR VALUES FROM ('2028-01-01') TO ('2029-01-01');
CREATE TABLE tb_audit_log_2029 PARTITION OF tb_audit_log
    FOR VALUES FROM ('2029-01-01') TO ('2030-01-01');
CREATE TABLE tb_audit_log_2030 PARTITION OF tb_audit_log
    FOR VALUES FROM ('2030-01-01') TO ('2031-01-01');
CREATE TABLE tb_audit_log_2031 PARTITION OF tb_audit_log
    FOR VALUES FROM ('2031-01-01') TO ('2032-01-01');

-- 5. 기존 데이터 이동
INSERT INTO tb_audit_log (id, user_id, action, target_table, target_id, detail, ip_address, created_at)
SELECT id, user_id, action, target_table, target_id, detail, ip_address, created_at
FROM tb_audit_log_old;

-- 6. 데이터 건수 검증 (이동 전후 비교)
DO $$
DECLARE old_count BIGINT;
DECLARE new_count BIGINT;
BEGIN
  SELECT count(*) INTO old_count FROM tb_audit_log_old;
  SELECT count(*) INTO new_count FROM tb_audit_log;
  IF old_count <> new_count THEN
    RAISE EXCEPTION '데이터 이동 검증 실패: old=%, new=%', old_count, new_count;
  END IF;
END $$;

-- 7. 기존 테이블 삭제
DROP TABLE tb_audit_log_old;

-- 8. 인덱스 재생성 (파티션 테이블에 필요, 부모 인덱스에서 자동 상속)
CREATE INDEX idx_audit_log_created_at ON tb_audit_log (created_at);
CREATE INDEX idx_audit_log_user_id ON tb_audit_log (user_id);
CREATE INDEX idx_audit_log_action ON tb_audit_log (action);
