package com.navy.casualty.dead.repository;

import java.util.Optional;

import com.navy.casualty.dead.entity.Dead;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사망자 리포지토리.
 */
public interface DeadRepository extends JpaRepository<Dead, Long>, DeadRepositoryCustom {

    boolean existsByServiceNumber(String serviceNumber);

    boolean existsBySsnHash(String ssnHash);

    Optional<Dead> findByServiceNumber(String serviceNumber);
}
