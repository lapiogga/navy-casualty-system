package com.navy.casualty.code.repository;

import java.util.List;

import com.navy.casualty.code.entity.UnitCode;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 부대 코드 리포지토리.
 */
public interface UnitCodeRepository extends JpaRepository<UnitCode, Long> {

    List<UnitCode> findByParentIdIsNullOrderByUnitNameAsc();
}
