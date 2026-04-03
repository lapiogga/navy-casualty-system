package com.navy.casualty.code.repository;

import com.navy.casualty.code.entity.DeathType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeathTypeRepository extends JpaRepository<DeathType, Long> {
}
