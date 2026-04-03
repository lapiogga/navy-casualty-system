package com.navy.casualty.code.repository;

import com.navy.casualty.code.entity.RankCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankCodeRepository extends JpaRepository<RankCode, Long> {
    List<RankCode> findAllByOrderBySortOrderAsc();
}
