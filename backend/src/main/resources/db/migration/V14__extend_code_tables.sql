-- V14: 코드 테이블 확장 (장교 계급 추가 + 사망구분 기호 데이터)

-- 장교 계급 추가 (소위·중위는 V9에 이미 존재)
INSERT INTO TB_RANK_CODE (rank_name, rank_group, sort_order, created_by) VALUES
('대위', '장교', 11, 'SYSTEM'),
('소령', '장교', 12, 'SYSTEM'),
('중령', '장교', 13, 'SYSTEM'),
('대령', '장교', 14, 'SYSTEM'),
('준장', '장관급', 15, 'SYSTEM'),
('소장', '장관급', 16, 'SYSTEM'),
('중장', '장관급', 17, 'SYSTEM'),
('대장', '장관급', 18, 'SYSTEM'),
('준위', '준사관', 19, 'SYSTEM');

-- 사망구분 기호 코드 (D-04 확정 데이터)
INSERT INTO TB_DEATH_CODE (code_symbol, code_name, created_by) VALUES
('A', '전투 중 사망', 'SYSTEM'),
('B', '작전 중 순직', 'SYSTEM'),
('C', '교육훈련 중 사고사', 'SYSTEM'),
('D', '근무 중 사고사', 'SYSTEM'),
('E', '질병 사망', 'SYSTEM'),
('F', '자살', 'SYSTEM'),
('G', '교통사고 사망', 'SYSTEM'),
('H', '기타 사고사', 'SYSTEM'),
('I', '원인 미상', 'SYSTEM');
