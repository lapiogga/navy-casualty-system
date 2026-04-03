package com.navy.casualty.dead.repository;

import com.navy.casualty.dead.dto.DeadSearchRequest;
import com.navy.casualty.dead.entity.Dead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 사망자 QueryDSL 동적 검색 인터페이스.
 */
public interface DeadRepositoryCustom {
    Page<Dead> search(DeadSearchRequest request, Pageable pageable);
    List<Dead> searchAll(DeadSearchRequest request);
}
