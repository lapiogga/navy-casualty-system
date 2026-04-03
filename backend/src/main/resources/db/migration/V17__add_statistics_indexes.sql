-- V17: 통계 성능 인덱스 (STAT-07)
-- 신분별 집계 성능 인덱스
CREATE INDEX IF NOT EXISTS idx_dead_branch_id ON TB_DEAD(branch_id) WHERE deleted_at IS NULL;
-- 부대별 집계/명부 성능 인덱스
CREATE INDEX IF NOT EXISTS idx_dead_unit_id ON TB_DEAD(unit_id) WHERE deleted_at IS NULL;
-- 월별/연도별 집계 성능 인덱스
CREATE INDEX IF NOT EXISTS idx_dead_death_date ON TB_DEAD(death_date) WHERE deleted_at IS NULL;
