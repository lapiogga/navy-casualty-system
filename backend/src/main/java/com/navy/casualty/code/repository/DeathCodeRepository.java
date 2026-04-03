package com.navy.casualty.code.repository;

import com.navy.casualty.code.entity.DeathCode;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사망구분 기호 코드 리포지토리.
 */
public interface DeathCodeRepository extends JpaRepository<DeathCode, Long> {
}
