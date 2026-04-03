package com.navy.casualty.code.repository;

import java.util.List;

import com.navy.casualty.code.entity.RankCode;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 계급 코드 리포지토리.
 */
public interface RankCodeRepository extends JpaRepository<RankCode, Long> {

    List<RankCode> findAllByOrderBySortOrderAsc();
}
