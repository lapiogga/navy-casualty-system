package com.navy.casualty.audit.service;

import java.time.LocalDateTime;

import com.navy.casualty.audit.entity.AuditLogEntry;
import com.navy.casualty.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 감사 로그 서비스.
 * 감사 로그 저장 및 D-07 필터 기반 검색을 제공한다.
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * 감사 로그를 저장한다.
     * 호출측 트랜잭션과 독립적으로 새 트랜잭션에서 실행하여
     * 비즈니스 로직 실패 시에도 감사 로그가 기록되도록 한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLogEntry save(AuditLogEntry entry) {
        return auditLogRepository.save(entry);
    }

    /**
     * D-07 필터 기반 감사 로그 검색.
     * 기간, 사용자ID, 액션 3가지 조건을 조합하여 검색한다.
     */
    @Transactional(readOnly = true)
    public Page<AuditLogEntry> search(LocalDateTime startDate, LocalDateTime endDate,
                                      String userId, String action, Pageable pageable) {
        Specification<AuditLogEntry> spec = Specification.where(null);

        if (startDate != null && endDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("createdAt"), startDate, endDate));
        }

        if (userId != null && !userId.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("userId"), userId));
        }

        if (action != null && !action.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("action"), action));
        }

        return auditLogRepository.findAll(spec, pageable);
    }
}
