-- AUDIT-02: TB_AUDIT_LOG append-only 보호
-- UPDATE/DELETE 시도를 PostgreSQL RULE로 차단

CREATE RULE no_update_audit_log AS
    ON UPDATE TO TB_AUDIT_LOG DO INSTEAD NOTHING;

CREATE RULE no_delete_audit_log AS
    ON DELETE TO TB_AUDIT_LOG DO INSTEAD NOTHING;

-- AUDIT-03: 감사 로그 5년 이상 보관 정책
-- 개인정보보호법 시행령 제34조에 따라 감사 로그는 최소 5년 보관 필수.
-- 향후 데이터 증가 시 created_at 기준 RANGE 파티셔닝 도입 계획:
--   1) TB_AUDIT_LOG를 연도별 파티션 테이블로 전환
--   2) 5년 초과 파티션은 아카이브 테이블스페이스로 이동
--   3) pg_cron 등으로 파티션 자동 생성/관리
-- 현재는 단일 테이블로 운영하며, 보관 기한 내 DELETE는 RULE로 차단됨.
