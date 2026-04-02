-- V10: 초기 관리자 계정 (비밀번호: admin1234, BCrypt(12) 사전 해싱)
INSERT INTO TB_USER (username, password, name, role, enabled, account_locked, failed_login_count, created_by)
VALUES (
    'admin',
    '$2a$12$LJ3m4ys3uz2YHIxPM0Y5S.3TS8VGA6IjDg78B1sGFJpBksmB/VxDy',
    '시스템관리자',
    'ADMIN',
    TRUE,
    FALSE,
    0,
    'SYSTEM'
);
