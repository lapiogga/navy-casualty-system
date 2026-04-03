package com.navy.casualty.document.service;

import java.time.LocalDateTime;

import com.navy.casualty.audit.annotation.AuditLog;
import com.navy.casualty.document.dto.DocumentIssueResponse;
import com.navy.casualty.document.dto.DocumentIssueSearchRequest;
import com.navy.casualty.document.entity.DocumentIssue;
import com.navy.casualty.document.enums.DocumentType;
import com.navy.casualty.document.repository.DocumentIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 문서 발급 이력 서비스.
 * 발급 이력을 기록하고 검색한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentIssueService {

    private final DocumentIssueRepository documentIssueRepository;

    /**
     * 문서 발급 이력을 기록한다.
     */
    @AuditLog(action = "PRINT", targetTable = "TB_DOCUMENT_ISSUE")
    @Transactional
    public DocumentIssue recordIssue(DocumentType documentType, Long targetId, String purpose) {
        String username = getCurrentUsername();

        DocumentIssue issue = DocumentIssue.builder()
                .documentType(documentType)
                .targetTable(documentType.getTargetTable())
                .targetId(targetId)
                .issuePurpose(purpose)
                .issuedBy(username)
                .issuedAt(LocalDateTime.now())
                .build();

        return documentIssueRepository.save(issue);
    }

    /**
     * 문서 발급 이력을 검색한다 (동적 조건 + 페이징).
     */
    public Page<DocumentIssueResponse> search(DocumentIssueSearchRequest request, Pageable pageable) {
        Page<DocumentIssue> page = documentIssueRepository.search(request, pageable);
        return page.map(DocumentIssueResponse::from);
    }

    /**
     * 현재 인증된 사용자명을 반환한다.
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "system";
        }
        return auth.getName();
    }
}
