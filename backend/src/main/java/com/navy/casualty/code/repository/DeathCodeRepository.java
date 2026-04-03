package com.navy.casualty.code.repository;

import com.navy.casualty.code.entity.DeathCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeathCodeRepository extends JpaRepository<DeathCode, Long> {
}
