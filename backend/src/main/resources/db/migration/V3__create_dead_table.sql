-- V3: 사망자 테이블 생성 (DEAD-01~07)

CREATE TABLE TB_DEAD (
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
    death_type_id BIGINT REFERENCES TB_DEATH_TYPE(id),
    death_code_id BIGINT REFERENCES TB_DEATH_CODE(id),
    address TEXT,
    death_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    last_modified_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    delete_reason VARCHAR(500)
);

-- status: REGISTERED, CONFIRMED, NOTIFIED

CREATE INDEX idx_dead_birth_date ON TB_DEAD(birth_date);
CREATE INDEX idx_dead_name ON TB_DEAD(name);
CREATE INDEX idx_dead_status ON TB_DEAD(status);
