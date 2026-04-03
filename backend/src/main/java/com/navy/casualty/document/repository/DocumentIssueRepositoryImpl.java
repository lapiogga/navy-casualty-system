package com.navy.casualty.document.repository;

import java.util.List;

import com.navy.casualty.document.dto.DocumentIssueSearchRequest;
import com.navy.casualty.document.entity.DocumentIssue;
import com.navy.casualty.document.entity.QDocumentIssue;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * QueryDSL 기반 문서 발급 이력 동적 검색 구현.
 */
@RequiredArgsConstructor
public class DocumentIssueRepositoryImpl implements DocumentIssueRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<DocumentIssue> search(DocumentIssueSearchRequest request, Pageable pageable) {
        QDocumentIssue issue = QDocumentIssue.documentIssue;
        BooleanBuilder where = buildConditions(request, issue);

        List<DocumentIssue> content = queryFactory
                .selectFrom(issue)
                .where(where)
                .orderBy(issue.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(issue.count())
                .from(issue)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<DocumentIssue> searchAll(DocumentIssueSearchRequest request) {
        QDocumentIssue issue = QDocumentIssue.documentIssue;
        BooleanBuilder where = buildConditions(request, issue);

        return queryFactory
                .selectFrom(issue)
                .where(where)
                .orderBy(issue.id.desc())
                .fetch();
    }

    /**
     * 검색 조건을 BooleanBuilder로 구성한다.
     */
    private BooleanBuilder buildConditions(DocumentIssueSearchRequest request, QDocumentIssue issue) {
        BooleanBuilder builder = new BooleanBuilder();

        if (request.documentType() != null) {
            builder.and(issue.documentType.eq(request.documentType()));
        }
        if (request.issuedBy() != null && !request.issuedBy().isBlank()) {
            builder.and(issue.issuedBy.contains(request.issuedBy()));
        }
        if (request.startDate() != null) {
            builder.and(issue.issuedAt.goe(request.startDate().atStartOfDay()));
        }
        if (request.endDate() != null) {
            builder.and(issue.issuedAt.lt(request.endDate().plusDays(1).atStartOfDay()));
        }

        return builder;
    }
}
