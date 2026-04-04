-- V18: 실 코드 데이터 적재 (D-25)
-- 운영은 신규 DB 전제이므로 DELETE + INSERT 패턴

-- 계급 코드 실 데이터 (해군 계급 체계)
DELETE FROM TB_RANK_CODE;
INSERT INTO TB_RANK_CODE (rank_name, rank_group, sort_order, created_by) VALUES
('대장', '장관급장교', 1, 'SYSTEM'),
('중장', '장관급장교', 2, 'SYSTEM'),
('소장', '장관급장교', 3, 'SYSTEM'),
('준장', '장관급장교', 4, 'SYSTEM'),
('대령', '영관급장교', 5, 'SYSTEM'),
('중령', '영관급장교', 6, 'SYSTEM'),
('소령', '영관급장교', 7, 'SYSTEM'),
('대위', '위관급장교', 8, 'SYSTEM'),
('중위', '위관급장교', 9, 'SYSTEM'),
('소위', '위관급장교', 10, 'SYSTEM'),
('준위', '준위', 11, 'SYSTEM'),
('원사', '부사관', 12, 'SYSTEM'),
('상사', '부사관', 13, 'SYSTEM'),
('중사', '부사관', 14, 'SYSTEM'),
('하사', '부사관', 15, 'SYSTEM'),
('병장', '병', 16, 'SYSTEM'),
('상병', '병', 17, 'SYSTEM'),
('일병', '병', 18, 'SYSTEM'),
('이병', '병', 19, 'SYSTEM');

-- 군구분 코드 (해군 기준)
DELETE FROM TB_BRANCH_CODE;
INSERT INTO TB_BRANCH_CODE (branch_name, created_by) VALUES
('해군장교', 'SYSTEM'),
('해군부사관', 'SYSTEM'),
('해군병', 'SYSTEM'),
('해병장교', 'SYSTEM'),
('해병부사관', 'SYSTEM'),
('해병병', 'SYSTEM'),
('군무원', 'SYSTEM');

-- 사망유형 코드
DELETE FROM TB_DEATH_TYPE;
INSERT INTO TB_DEATH_TYPE (type_name, created_by) VALUES
('전사', 'SYSTEM'),
('전상사', 'SYSTEM'),
('순직', 'SYSTEM'),
('공상사', 'SYSTEM'),
('사고사', 'SYSTEM'),
('병사', 'SYSTEM'),
('자살', 'SYSTEM'),
('기타', 'SYSTEM');

-- 부대 코드 (해군 주요 부대)
DELETE FROM TB_UNIT_CODE;
INSERT INTO TB_UNIT_CODE (unit_name, parent_id, created_by) VALUES
('해군본부', NULL, 'SYSTEM'),
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
('해군사관학교', (SELECT id FROM TB_UNIT_CODE WHERE unit_name = '해군교육사령부'), 'SYSTEM'),
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

-- 보훈청/보훈지청 (전국)
DELETE FROM TB_VETERANS_OFFICE;
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
