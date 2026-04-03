package com.navy.casualty.wounded.repository;

import java.util.List;

import com.navy.casualty.wounded.dto.WoundedSearchRequest;
import com.navy.casualty.wounded.entity.Wounded;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 상이자 동적 검색 커스텀 리포지토리.
 */
public interface WoundedRepositoryCustom {

    Page<Wounded> search(WoundedSearchRequest request, Pageable pageable);

    List<Wounded> searchAll(WoundedSearchRequest request);
}
