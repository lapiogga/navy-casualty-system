package com.navy.casualty.code.repository;

import com.navy.casualty.code.entity.UnitCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnitCodeRepository extends JpaRepository<UnitCode, Long> {
    List<UnitCode> findByParentIdIsNullOrderByUnitNameAsc();
}
