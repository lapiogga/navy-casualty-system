# 해군 사상자 관리 시스템 -- 배포 절차서

버전: 1.0.0 | 작성일: 2026-04-04

---

## 1. 사전 요구사항

| 항목 | 최소 요건 |
|------|----------|
| 운영체제 | Linux (CentOS 7+, Ubuntu 20.04+, Rocky Linux 8+) |
| Docker Engine | 24.0 이상 |
| Docker Compose | v2 (docker compose 명령어 지원) |
| 디스크 | 최소 20GB 여유 공간 |
| 메모리 | 최소 4GB RAM (권장 8GB) |
| CPU | 2코어 이상 |
| 포트 | 8080(HTTP), 8443(HTTPS), 5432(DB, 내부만) |

> 군 내부망(에어갭) 환경이므로 외부 네트워크 접속이 불가합니다.
> 모든 패키지는 USB 또는 보안매체로 전달합니다.

---

## 2. 패키지 전달 및 압축 해제

### 2.1 패키지 구성

```
navy-casualty-deploy-v1.0.0.tar.gz
  ├── docker-compose.yml
  ├── docker-compose.prod.yml
  ├── .env.example
  ├── deploy/
  │   ├── deploy.sh
  │   ├── generate-cert.sh
  │   ├── backup.sh
  │   ├── restore.sh
  │   ├── package.sh
  │   └── archive-audit-log.sh
  ├── images/
  │   ├── app.tar.gz          # Spring Boot 앱 이미지
  │   └── db.tar.gz           # PostgreSQL 16 이미지
  └── certs/                   # TLS 인증서 디렉토리 (빈 폴더)
```

### 2.2 압축 해제

```bash
# USB 마운트 후 패키지 복사
cp /mnt/usb/navy-casualty-deploy-v1.0.0.tar.gz /opt/navy-casualty/

# 압축 해제
cd /opt/navy-casualty
tar xzf navy-casualty-deploy-v1.0.0.tar.gz
cd navy-casualty-deploy-v1.0.0
```

---

## 3. Docker 이미지 로드

```bash
# 자동 로드 (deploy.sh 사용)
./deploy/deploy.sh

# 또는 수동 로드
docker load < images/app.tar.gz
docker load < images/db.tar.gz

# 이미지 확인
docker images | grep navy-casualty
```

---

## 4. 환경변수 설정

### 4.1 .env 파일 생성

```bash
cp .env.example .env
vi .env
```

### 4.2 환경변수 설명

| 변수 | 설명 | 필수 | 기본값 |
|------|------|------|--------|
| `DB_NAME` | 데이터베이스 이름 | O | `navy_casualty` |
| `DB_USERNAME` | 데이터베이스 사용자명 | O | `navy_user` |
| `DB_PASSWORD` | 데이터베이스 비밀번호 | **필수** | - |
| `DB_PORT` | 데이터베이스 포트 | O | `5432` |
| `APP_PORT` | 애플리케이션 HTTP 포트 | O | `8080` |
| `SPRING_PROFILE` | 스프링 프로파일 | O | `prod` |
| `PII_ENCRYPTION_KEY` | 개인정보 암호화 키 (AES-256) | **필수** | - |
| `SERVER_SSL_ENABLED` | TLS 활성화 여부 | O | `false` |
| `SSL_KEYSTORE_PASSWORD` | TLS 키스토어 비밀번호 | 조건부 | - |

### 4.3 비밀번호 정책

- `DB_PASSWORD`: 최소 16자, 영문 대소문자 + 숫자 + 특수문자 포함 권장
- `PII_ENCRYPTION_KEY`: AES-256 호환 키 (32바이트). 분실 시 암호화된 데이터 복구 불가
- `SSL_KEYSTORE_PASSWORD`: TLS 활성화 시에만 필요

> **경고**: `PII_ENCRYPTION_KEY`는 운영 환경에서 반드시 고유한 값을 설정하십시오.
> 이 키를 변경하면 기존 암호화된 주민등록번호 데이터를 복호화할 수 없습니다.

---

## 5. TLS 인증서 설정

### 5.1 자체 서명 인증서 생성

```bash
./deploy/generate-cert.sh
```

생성 결과: `certs/keystore.p12` 파일 생성

### 5.2 CA 발급 인증서 사용

기관 인증기관(CA)에서 발급받은 인증서를 사용하는 경우:

```bash
# PKCS#12 형식으로 변환
openssl pkcs12 -export \
  -in server.crt \
  -inkey server.key \
  -out certs/keystore.p12 \
  -name navy-casualty \
  -passout pass:${SSL_KEYSTORE_PASSWORD}
```

### 5.3 TLS 활성화

`.env` 파일에 다음을 설정:

```
SERVER_SSL_ENABLED=true
SSL_KEYSTORE_PASSWORD=<키스토어 비밀번호>
```

---

## 6. 서비스 시작

### 6.1 자동 시작 (권장)

```bash
./deploy/deploy.sh
```

### 6.2 수동 시작

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### 6.3 시작 확인

```bash
# 컨테이너 상태 확인
docker compose ps

# 로그 실시간 확인 (최초 기동 시 Flyway 마이그레이션 포함)
docker compose logs -f app
```

> 최초 기동 시 Flyway가 자동으로 DB 스키마를 생성합니다. 약 30초~1분 소요됩니다.

---

## 7. 헬스체크 확인

```bash
# HTTP 헬스체크
curl http://localhost:8080/actuator/health
# 정상 응답: {"status":"UP"}

# HTTPS 사용 시 (자체 서명 인증서)
curl -k https://localhost:8443/actuator/health

# Docker 헬스체크 상태
docker compose ps
# STATUS 열에 "healthy" 표시 확인
```

---

## 8. 초기 설정

### 8.1 기본 관리자 계정

| 항목 | 값 |
|------|-----|
| ID | `admin` |
| 초기 비밀번호 | `admin` |
| 역할 | `ADMIN` |

> **중요**: 첫 로그인 시 비밀번호 변경이 강제됩니다. 반드시 강력한 비밀번호로 변경하십시오.

### 8.2 코드 테이블 정합성 확인

```bash
curl http://localhost:8080/api/admin/data-check
```

코드 테이블(군구분, 계급, 부대코드 등)의 데이터 정합성을 검증합니다.

---

## 9. 네트워크 구성도

```
                         방화벽
                           |
[사용자 PC (브라우저)] ──HTTPS:8443──> [Docker Host]
                                         |
                                  ┌──────┴──────┐
                                  │             │
                            [App Container]  [DB Container]
                            (Spring Boot)    (PostgreSQL 16)
                                  │             │
                                  └──5432(내부)──┘
                                  Docker Network
```

- 사용자는 HTTPS(8443) 포트로만 접속
- DB 컨테이너는 Docker 내부 네트워크에서만 접근 가능
- 외부에서 DB 포트(5432) 직접 접근 불가 (운영 환경 설정)

---

## 10. 방화벽 규칙

### 10.1 인바운드(INBOUND) 규칙

| 포트 | 프로토콜 | 용도 | 허용 범위 |
|------|----------|------|----------|
| 8443 | TCP | HTTPS (주 접속 포트) | 내부망 전체 |
| 8080 | TCP | HTTP (HTTPS 리다이렉트용) | 내부망 전체 |

### 10.2 아웃바운드(OUTBOUND) 규칙

- **없음** (에어갭 환경 -- 외부 네트워크 연결 불가)

### 10.3 확인 명령

```bash
# DB 포트 외부 차단 확인
ss -tlnp | grep 5432
# Docker 내부 네트워크에서만 리스닝 확인

# 방화벽 규칙 확인 (firewalld)
firewall-cmd --list-all

# 방화벽 규칙 확인 (iptables)
iptables -L -n | grep -E "8080|8443|5432"
```

---

## 11. 백업/복원 절차

### 11.1 일일 백업

```bash
# 수동 백업
./deploy/backup.sh

# cron 등록 (매일 02:00 자동 백업)
echo "0 2 * * * /opt/navy-casualty/deploy/backup.sh >> /var/log/navy-backup.log 2>&1" | crontab -
```

백업 파일 위치: `./backups/navy_casualty_YYYYMMDD_HHMMSS.sql.gz`

### 11.2 복원

```bash
./deploy/restore.sh <백업파일경로>

# 예시
./deploy/restore.sh ./backups/navy_casualty_20260404_020000.sql.gz
```

### 11.3 백업 권장 사항

- 백업 파일을 외부 보안매체(USB, 외장하드)에 이중 보관
- 주 1회 복원 테스트 수행 권장
- 백업 파일 보관 기간: 최소 90일

---

## 12. 감사 로그 아카이브

감사 로그는 시스템의 모든 활동(조회, 등록, 수정, 삭제, 출력)을 기록합니다.

### 12.1 연간 아카이브

```bash
# 5년 초과 파티션 아카이브
./deploy/archive-audit-log.sh
```

### 12.2 아카이브 정책

- 연 1회 아카이브 실행 권장 (연초)
- 5년 초과 감사 로그 파티션을 별도 파일로 추출
- 아카이브 완료 후 해당 파티션 삭제 가능
- 아카이브 파일은 보안매체에 장기 보관

---

## 13. 모니터링

### 13.1 헬스체크

```bash
# 애플리케이션 상태
curl http://localhost:8080/actuator/health
# 정상: {"status":"UP"}
```

### 13.2 로그 확인

```bash
# 실시간 로그
docker compose logs -f app

# 최근 100줄
docker compose logs --tail=100 app
```

로그 파일 위치 (호스트 마운트): `./logs/navy-casualty.log`

### 13.3 디스크 사용량

```bash
# 호스트 디스크
df -h

# Docker 디스크
docker system df
```

### 13.4 주요 모니터링 항목

| 항목 | 명령어 | 정상 기준 |
|------|--------|----------|
| 앱 상태 | `curl .../actuator/health` | `{"status":"UP"}` |
| 컨테이너 상태 | `docker compose ps` | STATUS: healthy |
| 디스크 여유 | `df -h` | 20% 이상 여유 |
| 메모리 사용 | `docker stats --no-stream` | 앱: 512MB~1GB |

---

## 14. 업데이트 절차

### 14.1 업데이트 패키지 전달

새 버전 패키지를 USB/보안매체로 전달받습니다.

### 14.2 업데이트 수행

```bash
# 1. 사전 백업 (필수)
./deploy/backup.sh

# 2. 서비스 중지
docker compose -f docker-compose.yml -f docker-compose.prod.yml down

# 3. 새 이미지 로드
docker load < images/app.tar.gz

# 4. 서비스 재시작
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 5. 헬스체크 확인
curl http://localhost:8080/actuator/health
```

> Flyway가 자동으로 DB 스키마 마이그레이션을 수행합니다.
> 별도의 DB 스키마 변경 작업은 불필요합니다.

---

## 15. 장애 판단 및 대응

| 증상 | 원인 | 대응 |
|------|------|------|
| healthcheck 실패 (`{"status":"DOWN"}`) | 앱 비정상 종료 또는 DB 연결 실패 | `docker compose restart app` |
| Connection refused (접속 불가) | 앱 컨테이너 미기동 | `docker compose logs app` 확인 후 `docker compose up -d` |
| OOM (OutOfMemoryError) | JVM 힙 메모리 부족 | `.env`에 `JAVA_OPTS=-Xmx2g` 추가 후 재시작 |
| disk full (디스크 부족) | 로그/백업 누적 | 로그 정리, 오래된 백업 삭제, `docker system prune` |
| DB 연결 실패 | DB 컨테이너 다운 | `docker compose restart db` 후 앱 재시작 |
| FlywayException (마이그레이션 실패) | 스키마 충돌 | 로그에서 실패 SQL 확인, 수동 해결 후 재시작 |
| PiiEncryption 오류 | 암호화 키 불일치 | `.env`의 `PII_ENCRYPTION_KEY` 확인 |
| 로그인 불가 (계정 잠금) | 5회 비밀번호 오류 | 관리자가 잠금 해제 또는 DB 직접 수정 |

---

## 16. 트러블슈팅

### 16.1 로그 위치

| 위치 | 경로 |
|------|------|
| 컨테이너 내부 | `/app/logs/navy-casualty.log` |
| 호스트 마운트 | `./logs/navy-casualty.log` |
| Docker 로그 | `docker compose logs app` |

### 16.2 주요 에러 키워드

| 키워드 | 의미 | 대응 |
|--------|------|------|
| `HikariPool` | DB 커넥션 풀 문제 | DB 컨테이너 상태 확인, 연결 정보 검증 |
| `FlywayException` | DB 마이그레이션 실패 | 마이그레이션 SQL 충돌 확인 |
| `PiiEncryption` | 개인정보 암호화/복호화 오류 | `PII_ENCRYPTION_KEY` 설정 확인 |
| `AuthenticationException` | 인증 실패 | 계정 잠금 여부, 세션 만료 확인 |
| `AccessDeniedException` | 권한 부족 | 사용자 역할 확인 (RBAC) |

### 16.3 긴급 DB 접속 (비상 시)

```bash
# DB 컨테이너에 직접 접속
docker compose exec db psql -U navy_user -d navy_casualty

# 계정 잠금 해제 (비상)
UPDATE tb_user SET account_locked = false, failed_login_attempts = 0 WHERE username = '<사용자명>';
```

### 16.4 전체 초기화 (최후 수단)

```bash
# 주의: 모든 데이터가 삭제됩니다
docker compose -f docker-compose.yml -f docker-compose.prod.yml down -v
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

> **경고**: 전체 초기화는 모든 데이터를 삭제합니다. 반드시 백업 후 수행하십시오.
