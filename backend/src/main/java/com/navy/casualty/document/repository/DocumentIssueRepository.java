package com.navy.casualty.document.repository;

import com.navy.casualty.document.entity.DocumentIssue;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 문서 발급 이력 리포지토리.
 */
public interface DocumentIssueRepository extends JpaRepository<DocumentIssue, Long>, DocumentIssueRepositoryCustom {
}
