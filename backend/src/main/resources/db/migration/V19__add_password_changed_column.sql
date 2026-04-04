-- V19: 비밀번호 변경 여부 플래그 (D-27)
ALTER TABLE TB_USER ADD COLUMN password_changed BOOLEAN NOT NULL DEFAULT false;
-- 기존 admin 계정도 false (첫 로그인 시 변경 강제)
