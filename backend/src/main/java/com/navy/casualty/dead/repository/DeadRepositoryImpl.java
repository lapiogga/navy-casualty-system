package com.navy.casualty.dead.repository;

import java.util.List;

import com.navy.casualty.dead.dto.DeadSearchRequest;
import com.navy.casualty.dead.entity.Dead;
import com.navy.casualty.dead.entity.QDead;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * QueryDSL 기반 사망자 동적 검색 구현.
 */
@RequiredArgsConstructor
public class DeadRepositoryImpl implements DeadRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Dead> search(DeadSearchRequest request, Pageable pageable) {
        QDead dead = QDead.dead;
        BooleanBuilder builder = new BooleanBuilder();

        if (request.branchId() != null) {
            builder.and(dead.branchId.eq(request.branchId()));
        }
        if (request.serviceNumber() != null && !request.serviceNumber().isBlank()) {
            builder.and(dead.serviceNumber.contains(request.serviceNumber()));
        }
        if (request.name() != null && !request.name().isBlank()) {
            builder.and(dead.name.contains(request.name()));
        }
        if (request.birthDate() != null) {
            builder.and(dead.birthDate.eq(request.birthDate()));
        }
        if (request.rankId() != null) {
            builder.and(dead.rankId.eq(request.rankId()));
        }
        if (request.unitId() != null) {
            builder.and(dead.unitId.eq(request.unitId()));
        }
        if (request.deathTypeId() != null) {
            builder.and(dead.deathTypeId.eq(request.deathTypeId()));
        }
        if (request.status() != null) {
            builder.and(dead.status.eq(request.status()));
        }

        List<Dead> content = queryFactory
                .selectFrom(dead)
                .where(builder)
                .orderBy(dead.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(dead.count())
                .from(dead)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
