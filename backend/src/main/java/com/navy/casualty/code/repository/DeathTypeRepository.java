package com.navy.casualty.code.repository;

import com.navy.casualty.code.entity.DeathType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사망유형 코드 리포지토리.
 */
public interface DeathTypeRepository extends JpaRepository<DeathType, Long> {
}
