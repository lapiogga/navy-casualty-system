package com.navy.casualty.document.repository;

import java.util.List;

import com.navy.casualty.document.dto.DocumentIssueSearchRequest;
import com.navy.casualty.document.entity.DocumentIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 문서 발급 이력 동적 검색 커스텀 리포지토리.
 */
public interface DocumentIssueRepositoryCustom {

    Page<DocumentIssue> search(DocumentIssueSearchRequest request, Pageable pageable);

    List<DocumentIssue> searchAll(DocumentIssueSearchRequest request);
}
