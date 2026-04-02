-- V4: 상이자 테이블 생성 (WOND-01~07)

CREATE TABLE TB_WOUNDED (
    id BIGSERIAL PRIMARY KEY,
    service_number VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    ssn_encrypted TEXT NOT NULL,
    birth_date DATE NOT NULL,
    rank_id BIGINT REFERENCES TB_RANK_CODE(id),
    branch_id BIGINT REFERENCES TB_BRANCH_CODE(id),
    unit_id BIGINT REFERENCES TB_UNIT_CODE(id),
    enlistment_date DATE,
    phone VARCHAR(20),
    address TEXT,
    veterans_office_id BIGINT REFERENCES TB_VETERANS_OFFICE(id),
    disease_name VARCHAR(200),
    wound_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    last_modified_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    delete_reason VARCHAR(500)
);

-- wound_type: COMBAT_WOUND(전공상), DUTY_WOUND(공상), GENERAL_WOUND(일반상이)
-- status: REGISTERED, UNDER_REVIEW, CONFIRMED, NOTIFIED

CREATE INDEX idx_wounded_birth_date ON TB_WOUNDED(birth_date);
CREATE INDEX idx_wounded_name ON TB_WOUNDED(name);
CREATE INDEX idx_wounded_status ON TB_WOUNDED(status);
