# 해군 사상자 관리 전산 시스템 (Naval Casualty Management System)

## What This Is

해군 내 사망자, 상이자, 전공사상심사 대상자의 정보를 체계적으로 등록·관리하고 공식 문서를 출력하는 군 내부망 전용 전산 시스템이다. 담당자가 신속하게 인원 현황을 조회하고, 국가유공자 확인서·순직확인서 등 공식 양식 문서를 발급하며, 통계 현황을 보고서로 산출할 수 있도록 지원한다.

## Core Value

데이터의 정확성과 접근 통제 — 군 인사기록 특성상 허가된 담당자만 민감한 개인정보에 접근할 수 있어야 하며, 한 번 등록된 기록은 감사 추적이 가능해야 한다.

## Requirements

### Validated

(None yet — ship to validate)

### Active

#### 사망자 관리
- [ ] 사망자 현황 조회 (군번, 성명, 생년월일, 계급, 소속으로 검색)
- [ ] 사망자 정보 등록 (군번, 성명, 주민등록번호, 계급, 소속, 입대일자, 전화번호, 사망구분, 사망구분 기호, 주소)
- [ ] 사망자 정보 수정
- [ ] 사망자 정보 삭제 (논리 삭제 — 감사 이력 보존)
- [ ] 사망자 현황 Excel 다운로드

#### 상이자 관리
- [ ] 상이자 현황 조회 (군번, 성명, 생년월일, 계급, 소속으로 검색)
- [ ] 상이자 정보 등록 (군번, 성명, 주민등록번호, 계급, 소속, 입대일자, 전화번호, 현주소, 보훈청명, 병명, 전공상 여부)
- [ ] 상이자 정보 수정
- [ ] 상이자 정보 삭제 (논리 삭제)
- [ ] 상이자 현황 Excel 다운로드

#### 전공사상심사 관리
- [ ] 전공사상심사 현황 조회 (군번, 성명, 생년월일, 계급, 소속으로 검색)
- [ ] 전공사상심사 정보 등록 (심사차수, 심사일자, 성명, 군번, 주민번호, 입대일자, 소속, 병명, 소속부대 심사결과, 전공상 분류)
- [ ] 전공사상심사 정보 수정
- [ ] 전공사상심사 정보 삭제 (논리 삭제)
- [ ] 전공사상심사 현황 Excel 다운로드
- [ ] 전공사상심사 이력 조회 (심사차수별)

#### 공식 문서 출력
- [ ] 국가유공자 요건 해당사실 확인서 (사망자용) 출력
- [ ] 국가유공자 요건 해당사실 확인서 (상이자용) 출력
- [ ] 전공사상심사결과서 출력
- [ ] 순직/사망확인서 출력
- [ ] 사망자 현황 보고서 출력
- [ ] 상이자 현황 보고서 출력
- [ ] 전사망자 확인증 발급대장 출력
- [ ] 문서 발급 이력 자동 기록 (발급 목적 입력 필수)

#### 통계 / 현황
- [ ] 신분별 사망자 현황 조회/Excel
- [ ] 월별 사망자 현황 조회/Excel
- [ ] 연도별 사망자 현황 조회/Excel
- [ ] 부대별 사망자 현황 조회/Excel
- [ ] 부대별 사망자 명부 조회/Excel
- [ ] 전사망자 명부 조회/Excel

#### 시스템 공통
- [ ] 역할 기반 접근 제어 (RBAC) — 역할별 메뉴/기능 제한
- [ ] 세션 기반 인증 (로그인 실패 잠금, 타임아웃)
- [ ] 전체 감사 로그 (조회/등록/수정/삭제/출력 전 이력 기록)
- [ ] 주민등록번호 마스킹 표시 (역할에 따른 단계별 노출)

### Out of Scope

- 외부 인터넷 연동 — 군 내부망 전용 배포, 외부 API 연동 없음
- 모바일 앱 — 데스크톱 브라우저 전용 (내부망 환경)
- 실시간 알림/Push — 단순 조회·등록 시스템, 실시간 이벤트 불필요
- 육·공군·해병대 데이터 — 해군 전용 시스템, 타군 데이터 관리 제외
- 급여/보상 처리 — 보훈 급여 연산·지급은 별도 보훈 시스템 역할

## Context

- **완전 신규 구축**: 기존 전산 시스템 없음. Excel 등 수작업 관리에서 전환
- **해군 전용**: 해군 군구분 코드, 계급 체계, 부대 편제에 맞춤
- **군 내부망(인트라넷) 배포**: 외부 인터넷 차단 환경. Docker 기반 패키징으로 이식성 확보
- **현행 공식 문서 양식 존재**: 국가유공자 확인서, 순직확인서 등 해군 공식 양식 PDF/Word 형태로 제공 예정. Jasper Reports 템플릿으로 구현
- **민감 개인정보 처리**: 주민등록번호, 군번, 전화번호, 주소 포함. DB 레벨 암호화 적용 + 화면 마스킹

## Constraints

- **Tech Stack**: Java 21 + Spring Boot 3.x — 군/정부 시스템 표준 스택
- **Frontend**: React 18 + TypeScript + Ant Design — 복잡한 테이블/폼 UI 지원
- **Database**: PostgreSQL 16 — pgcrypto 지원, 논리 삭제 감사 이력
- **Deployment**: 군 내부망 전용, Docker Compose로 패키징
- **Security**: DB 레벨 암호화, RBAC, 세션 관리, 감사 로그 의무화
- **Document Output**: 현행 해군 공식 양식 기준 — 양식 원본 제공 후 Jasper Reports 템플릿 제작

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Java/Spring Boot 채택 | 군/정부 시스템 표준 스택, 보안 프레임워크 성숙도 | — Pending |
| 논리 삭제 정책 | 군 인사기록 특성상 물리 삭제 금지, 삭제 사유 필수 기록 | — Pending |
| 세션 기반 인증 (JWT 미사용) | 즉각적인 세션 무효화(계정 잠금) 가능, 내부망 환경에 적합 | — Pending |
| DB 레벨 암호화 | 보안 정책 기준, 추후 컬럼 레벨 AES-256 전환 가능하도록 설계 | — Pending |
| 해군 단독 배포 | 타군 데이터 혼재 방지, 단순한 코드 체계 유지 | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd:transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd:complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-03-31 after initialization*
