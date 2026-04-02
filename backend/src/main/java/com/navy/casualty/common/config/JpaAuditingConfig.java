package com.navy.casualty.common.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정.
 * Phase 1에서는 "SYSTEM" 고정 반환.
 * Phase 2에서 인증 사용자로 교체 예정.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // Phase 2에서 SecurityContextHolder 기반으로 교체
        return () -> Optional.of("SYSTEM");
    }
}
