package com.navy.casualty.code.repository;

import com.navy.casualty.code.entity.BranchCode;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 군구분 코드 리포지토리.
 */
public interface BranchCodeRepository extends JpaRepository<BranchCode, Long> {
}
