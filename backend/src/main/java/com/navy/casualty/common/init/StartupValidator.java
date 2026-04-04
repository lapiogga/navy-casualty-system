package com.navy.casualty.common.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 부팅 시 필수 환경변수, 코드 테이블, admin 계정을 검증한다.
 * 실패 시 로그 경고만 출력하고 정상 기동한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartupValidator implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        // 환경변수 검증
        checkEnv("DB_PASSWORD", "데이터베이스 비밀번호");
        checkEnv("PII_ENCRYPTION_KEY", "주민번호 암호화 키");

        // 코드 테이블 건수 검증
        try {
            Long rankCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tb_rank_code", Long.class);
            Long branchCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tb_branch_code", Long.class);
            log.info("부팅 검증 - 계급 코드: {}건, 군구분 코드: {}건", rankCount, branchCount);
            if (rankCount == 0 || branchCount == 0) {
                log.warn("코드 테이블이 비어 있습니다. 초기 데이터 적재가 필요합니다.");
            }
        } catch (Exception e) {
            log.warn("코드 테이블 조회 실패: {}", e.getMessage());
        }

        // admin 계정 확인
        try {
            Long adminCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tb_user WHERE username = 'admin'", Long.class);
            if (adminCount == 0) {
                log.warn("admin 계정이 없습니다.");
            }
        } catch (Exception e) {
            log.warn("admin 계정 조회 실패: {}", e.getMessage());
        }

        log.info("부팅 검증 완료");
    }

    private void checkEnv(String name, String description) {
        if (System.getenv(name) == null && System.getProperty(name) == null) {
            log.warn("환경변수 {} ({}) 가 설정되지 않았습니다", name, description);
        }
    }
}
