-- V12: admin 계정 BCrypt 해시 교정 (UAT Gap 1 - admin1234 해시 불일치 수정)
UPDATE TB_USER
SET password = '$2a$12$qE5eLh/.tsiHxqrItCKKWOWDAH/W94qu1ImrCPcGvUP8GdEF1fqxq'
WHERE username = 'admin';
