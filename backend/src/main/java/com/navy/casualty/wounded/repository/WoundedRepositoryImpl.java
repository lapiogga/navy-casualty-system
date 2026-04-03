package com.navy.casualty.wounded.repository;

import java.util.List;

import com.navy.casualty.wounded.dto.WoundedSearchRequest;
import com.navy.casualty.wounded.entity.QWounded;
import com.navy.casualty.wounded.entity.Wounded;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * QueryDSL 기반 상이자 동적 검색 구현.
 */
@RequiredArgsConstructor
public class WoundedRepositoryImpl implements WoundedRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Wounded> search(WoundedSearchRequest request, Pageable pageable) {
        QWounded wounded = QWounded.wounded;
        BooleanBuilder where = buildConditions(request, wounded);

        List<Wounded> content = queryFactory
                .selectFrom(wounded)
                .where(where)
                .orderBy(wounded.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(wounded.count())
                .from(wounded)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Wounded> searchAll(WoundedSearchRequest request) {
        QWounded wounded = QWounded.wounded;
        BooleanBuilder where = buildConditions(request, wounded);

        return queryFactory
                .selectFrom(wounded)
                .where(where)
                .orderBy(wounded.id.desc())
                .fetch();
    }

    /**
     * 검색 조건을 BooleanBuilder로 구성한다.
     */
    private BooleanBuilder buildConditions(WoundedSearchRequest request, QWounded wounded) {
        BooleanBuilder builder = new BooleanBuilder();

        if (request.branchId() != null) {
            builder.and(wounded.branchId.eq(request.branchId()));
        }
        if (request.serviceNumber() != null && !request.serviceNumber().isBlank()) {
            builder.and(wounded.serviceNumber.contains(request.serviceNumber()));
        }
        if (request.name() != null && !request.name().isBlank()) {
            builder.and(wounded.name.contains(request.name()));
        }
        if (request.birthDate() != null) {
            builder.and(wounded.birthDate.eq(request.birthDate()));
        }
        if (request.rankId() != null) {
            builder.and(wounded.rankId.eq(request.rankId()));
        }
        if (request.unitId() != null) {
            builder.and(wounded.unitId.eq(request.unitId()));
        }
        if (request.woundType() != null) {
            builder.and(wounded.woundType.eq(request.woundType()));
        }
        if (request.status() != null) {
            builder.and(wounded.status.eq(request.status()));
        }

        return builder;
    }
}
