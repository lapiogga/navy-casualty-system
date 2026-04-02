-- V2: 코드 테이블 생성 (계급, 군구분, 사망구분코드, 사망유형, 부대, 보훈청)

-- 계급 코드
CREATE TABLE TB_RANK_CODE (
    id BIGSERIAL PRIMARY KEY,
    rank_name VARCHAR(50) NOT NULL,
    rank_group VARCHAR(20) NOT NULL,
    sort_order INT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    last_modified_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    delete_reason VARCHAR(500)
);

-- 군구분 코드 (해군, 해병대)
CREATE TABLE TB_BRANCH_CODE (
    id BIGSERIAL PRIMARY KEY,
    branch_name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    last_modified_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    delete_reason VARCHAR(500)
);

-- 사망구분 기호 코드 (D-04: 구조만 생성, 데이터 비워둠)
CREATE TABLE TB_DEATH_CODE (
    id BIGSERIAL PRIMARY KEY,
    code_symbol VARCHAR(10) NOT NULL UNIQUE,
    code_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    last_modified_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    delete_reason VARCHAR(500)
);

-- 사망유형 (전사, 순직, 사고사, 병사, 자살, 기타)
CREATE TABLE TB_DEATH_TYPE (
    id BIGSERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    last_modified_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    delete_reason VARCHAR(500)
);

-- 부대 코드 (자기참조 계층)
CREATE TABLE TB_UNIT_CODE (
    id BIGSERIAL PRIMARY KEY,
    unit_name VARCHAR(100) NOT NULL,
    parent_id BIGINT REFERENCES TB_UNIT_CODE(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    last_modified_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    delete_reason VARCHAR(500)
);

-- 보훈청/보훈지청
CREATE TABLE TB_VETERANS_OFFICE (
    id BIGSERIAL PRIMARY KEY,
    office_name VARCHAR(100) NOT NULL,
    office_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    last_modified_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    delete_reason VARCHAR(500)
);
