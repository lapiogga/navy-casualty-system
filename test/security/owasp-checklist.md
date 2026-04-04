# OWASP Top 10 (2021) 보안 체크리스트

> 해군 사상자 관리 전산 시스템 보안 점검 체크리스트.
> 각 항목을 수동으로 확인하고 [x] 표시한다.

| # | Category | Check | Status | Notes |
|---|----------|-------|--------|-------|
| A01:2021 | Broken Access Control | RBAC @PreAuthorize 전 엔드포인트 적용 확인 | [ ] | SecurityConfig + 메서드 보안 |
| A01:2021 | Broken Access Control | VIEWER가 삭제/수정 불가 확인 | [ ] | OPERATOR 이상만 POST/PUT/DELETE |
| A01:2021 | Broken Access Control | ADMIN 전용 API(/api/admin/**) 접근 제어 확인 | [ ] | hasRole('ADMIN') |
| A02:2021 | Cryptographic Failures | 주민번호 AES-256-GCM 암호화 확인 | [ ] | PiiEncryptionConverter |
| A02:2021 | Cryptographic Failures | 주민번호 SHA-256 해시 별도 컬럼 확인 | [ ] | ssn_hash 컬럼 |
| A02:2021 | Cryptographic Failures | 비밀번호 BCrypt(12) 해싱 확인 | [ ] | PasswordEncoder 설정 |
| A02:2021 | Cryptographic Failures | HTTPS/TLS 적용 확인 (운영 환경) | [ ] | ssl.enabled 환경변수 전환 |
| A03:2021 | Injection | SQL Injection: JPA/QueryDSL 파라미터화 쿼리 사용 확인 | [ ] | 네이티브 쿼리 미사용 |
| A03:2021 | Injection | XSS: React 기본 이스케이핑 확인 | [ ] | dangerouslySetInnerHTML 미사용 |
| A03:2021 | Injection | CSRF: REST API이므로 CSRF 비활성화 적절성 확인 | [ ] | 세션 기반 인증 |
| A04:2021 | Insecure Design | 계정 잠금 (5회 실패) 확인 | [ ] | User.incrementFailCount() |
| A04:2021 | Insecure Design | 동일 에러 메시지 (잠금/실패 구분 없음) 확인 | [ ] | LOGIN_ERROR_MESSAGE 통일 |
| A04:2021 | Insecure Design | 논리 삭제 정책 (물리 삭제 금지) 확인 | [ ] | @SQLRestriction("deleted_at IS NULL") |
| A05:2021 | Security Misconfiguration | Spring Actuator /health만 노출 확인 | [ ] | application.yml 설정 |
| A05:2021 | Security Misconfiguration | 보안 헤더 (HSTS, X-Content-Type-Options) 확인 | [ ] | SecurityConfig headers() |
| A05:2021 | Security Misconfiguration | X-Frame-Options DENY 확인 | [ ] | frameOptions(frame -> frame.deny()) |
| A05:2021 | Security Misconfiguration | 에러 응답에 스택트레이스 미노출 확인 | [ ] | GlobalExceptionHandler |
| A06:2021 | Vulnerable Components | npm audit 취약점 점검 | [ ] | HIGH/CRITICAL 0건 목표 |
| A06:2021 | Vulnerable Components | Gradle 의존성 취약점 점검 | [ ] | dependency-check 플러그인 |
| A07:2021 | Auth Failures | 세션 타임아웃 30분 확인 | [ ] | server.servlet.session.timeout |
| A07:2021 | Auth Failures | 동시 세션 1개 제한 확인 | [ ] | maximumSessions(1) |
| A07:2021 | Auth Failures | 초기 비밀번호 변경 강제 확인 | [ ] | PasswordGuard 컴포넌트 |
| A08:2021 | Software/Data Integrity | Flyway 마이그레이션 체인 무결성 확인 | [ ] | V1 ~ V14 순차 검증 |
| A08:2021 | Software/Data Integrity | Docker 이미지 무결성 확인 | [ ] | docker-compose.yml 버전 고정 |
| A09:2021 | Logging/Monitoring | 감사 로그 기록 확인 (로그인, CRUD, 출력) | [ ] | AuditLogAspect |
| A09:2021 | Logging/Monitoring | 감사 로그 append-only 확인 | [ ] | UPDATE/DELETE 차단 |
| A09:2021 | Logging/Monitoring | 감사 로그 파티셔닝 확인 | [ ] | V20 마이그레이션 |
| A10:2021 | SSRF | 서버에서 외부 URL 호출 없음 확인 (에어갭) | [x] | N/A - 군 내부망 전용 |

## 점검 절차

1. 소스 코드 리뷰: 위 항목별 관련 파일을 직접 확인
2. 동작 검증: 실제 환경에서 API 호출하여 확인
3. ZAP 자동 스캔: `bash test/security/owasp-zap-scan.sh` 실행
4. 결과 기록: Status 컬럼에 [x] 표시, Notes에 확인 내용 기록

## 참고 파일

| 항목 | 관련 파일 |
|------|----------|
| RBAC | `backend/src/main/java/com/navy/casualty/security/SecurityConfig.java` |
| 암호화 | `backend/src/main/java/com/navy/casualty/common/crypto/PiiEncryptionConverter.java` |
| 감사 로그 | `backend/src/main/java/com/navy/casualty/audit/aspect/AuditLogAspect.java` |
| 세션 관리 | `backend/src/main/resources/application.yml` |
| 보안 헤더 | `backend/src/main/java/com/navy/casualty/security/SecurityConfig.java` |
