-- V9: Mock 초기 데이터 삽입 (D-03)

-- 계급 코드 (10개)
INSERT INTO TB_RANK_CODE (rank_name, rank_group, sort_order, created_by) VALUES
('이등병', '병', 1, 'SYSTEM'),
('일등병', '병', 2, 'SYSTEM'),
('상등병', '병', 3, 'SYSTEM'),
('병장', '병', 4, 'SYSTEM'),
('하사', '부사관', 5, 'SYSTEM'),
('중사', '부사관', 6, 'SYSTEM'),
('상사', '부사관', 7, 'SYSTEM'),
('원사', '부사관', 8, 'SYSTEM'),
('소위', '장교', 9, 'SYSTEM'),
('중위', '장교', 10, 'SYSTEM');

-- 군구분 코드 (2개)
INSERT INTO TB_BRANCH_CODE (branch_name, created_by) VALUES
('해군', 'SYSTEM'),
('해병대', 'SYSTEM');

-- 사망유형 (6개)
INSERT INTO TB_DEATH_TYPE (type_name, created_by) VALUES
('전사', 'SYSTEM'),
('순직', 'SYSTEM'),
('사고사', 'SYSTEM'),
('병사', 'SYSTEM'),
('자살', 'SYSTEM'),
('기타', 'SYSTEM');

-- TB_DEATH_CODE: 데이터 삽입 없음 (D-04 -- 공식 코드 미확정)

-- 부대 코드 (20개 Mock 데이터)
INSERT INTO TB_UNIT_CODE (unit_name, parent_id, created_by) VALUES
('해군작전사령부', NULL, 'SYSTEM'),
('해군교육사령부', NULL, 'SYSTEM'),
('해군군수사령부', NULL, 'SYSTEM'),
('해병대사령부', NULL, 'SYSTEM');

INSERT INTO TB_UNIT_CODE (unit_name, parent_id, created_by) VALUES
('제1함대사령부', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해군작전사령부'), 'SYSTEM'),
('제2함대사령부', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해군작전사령부'), 'SYSTEM'),
('제3함대사령부', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해군작전사령부'), 'SYSTEM'),
('잠수함사령부', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해군작전사령부'), 'SYSTEM'),
('해군항공사령부', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해군작전사령부'), 'SYSTEM'),
('특수전전단', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해군작전사령부'), 'SYSTEM');

INSERT INTO TB_UNIT_CODE (unit_name, parent_id, created_by) VALUES
('해군기초군사교육단', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해군교육사령부'), 'SYSTEM'),
('해군부사관학교', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해군교육사령부'), 'SYSTEM'),
('해군기술행정학교', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해군교육사령부'), 'SYSTEM');

INSERT INTO TB_UNIT_CODE (unit_name, parent_id, created_by) VALUES
('해군보급창', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해군군수사령부'), 'SYSTEM'),
('해군정비창', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해군군수사령부'), 'SYSTEM');

INSERT INTO TB_UNIT_CODE (unit_name, parent_id, created_by) VALUES
('해병대제1사단', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해병대사령부'), 'SYSTEM'),
('해병대제2사단', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해병대사령부'), 'SYSTEM'),
('해병대제9여단', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해병대사령부'), 'SYSTEM'),
('해병대교육훈련단', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해병대사령부'), 'SYSTEM'),
('해병대군수단', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해병대사령부'), 'SYSTEM');

-- 보훈청/보훈지청 (15개)
INSERT INTO TB_VETERANS_OFFICE (office_name, office_type, created_by) VALUES
('서울지방보훈청', '보훈청', 'SYSTEM'),
('부산지방보훈청', '보훈청', 'SYSTEM'),
('대구지방보훈청', '보훈청', 'SYSTEM'),
('광주지방보훈청', '보훈청', 'SYSTEM'),
('대전지방보훈청', '보훈청', 'SYSTEM'),
('인천보훈지청', '보훈지청', 'SYSTEM'),
('수원보훈지청', '보훈지청', 'SYSTEM'),
('춘천보훈지청', '보훈지청', 'SYSTEM'),
('청주보훈지청', '보훈지청', 'SYSTEM'),
('전주보훈지청', '보훈지청', 'SYSTEM'),
('제주보훈지청', '보훈지청', 'SYSTEM'),
('창원보훈지청', '보훈지청', 'SYSTEM'),
('울산보훈지청', '보훈지청', 'SYSTEM'),
('안양보훈지청', '보훈지청', 'SYSTEM'),
('의정부보훈지청', '보훈지청', 'SYSTEM');
