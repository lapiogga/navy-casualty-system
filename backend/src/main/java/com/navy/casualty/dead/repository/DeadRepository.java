package com.navy.casualty.dead.repository;

import com.navy.casualty.dead.entity.Dead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadRepository extends JpaRepository<Dead, Long>, DeadRepositoryCustom {
    boolean existsByServiceNumber(String serviceNumber);
    boolean existsBySsnHash(String ssnHash);
}
