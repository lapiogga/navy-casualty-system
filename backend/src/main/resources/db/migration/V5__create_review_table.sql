-- V5: 전공사상심사 테이블 생성 (REVW-01~08)

CREATE TABLE TB_REVIEW (
    id BIGSERIAL PRIMARY KEY,
    review_round INT NOT NULL,
    review_date DATE,
    name VARCHAR(50) NOT NULL,
    service_number VARCHAR(20) NOT NULL,
    ssn_encrypted TEXT,
    birth_date DATE,
    enlistment_date DATE,
    unit_id BIGINT REFERENCES TB_UNIT_CODE(id),
    disease_name VARCHAR(200),
    unit_review_result VARCHAR(20),
    classification VARCHAR(20),
    notification_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    last_modified_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    delete_reason VARCHAR(500)
);

-- classification: COMBAT_WOUND, DUTY_WOUND, REJECTED, DEFERRED

CREATE INDEX idx_review_service_number ON TB_REVIEW(service_number);
CREATE INDEX idx_review_round ON TB_REVIEW(review_round);

-- 심사차수별 변경 이력 보존
CREATE TABLE TB_REVIEW_HISTORY (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL REFERENCES TB_REVIEW(id),
    review_round INT NOT NULL,
    snapshot JSONB NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    changed_by VARCHAR(50)
);

CREATE INDEX idx_review_history_review_id ON TB_REVIEW_HISTORY(review_id);
