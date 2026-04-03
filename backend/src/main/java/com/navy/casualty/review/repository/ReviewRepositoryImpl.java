package com.navy.casualty.review.repository;

import java.util.List;

import com.navy.casualty.review.dto.ReviewSearchRequest;
import com.navy.casualty.review.entity.QReview;
import com.navy.casualty.review.entity.Review;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * QueryDSL 기반 전공사상심사 동적 검색 구현.
 */
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Review> search(ReviewSearchRequest request, Pageable pageable) {
        QReview review = QReview.review;
        BooleanBuilder where = buildConditions(request, review);

        List<Review> content = queryFactory
                .selectFrom(review)
                .where(where)
                .orderBy(review.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(review.count())
                .from(review)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Review> searchAll(ReviewSearchRequest request) {
        QReview review = QReview.review;
        BooleanBuilder where = buildConditions(request, review);

        return queryFactory
                .selectFrom(review)
                .where(where)
                .orderBy(review.id.desc())
                .fetch();
    }

    /**
     * 검색 조건을 BooleanBuilder로 구성한다.
     */
    private BooleanBuilder buildConditions(ReviewSearchRequest request, QReview review) {
        BooleanBuilder builder = new BooleanBuilder();

        if (request.branchId() != null) {
            builder.and(review.branchId.eq(request.branchId()));
        }
        if (request.serviceNumber() != null && !request.serviceNumber().isBlank()) {
            builder.and(review.serviceNumber.contains(request.serviceNumber()));
        }
        if (request.name() != null && !request.name().isBlank()) {
            builder.and(review.name.contains(request.name()));
        }
        if (request.birthDate() != null) {
            builder.and(review.birthDate.eq(request.birthDate()));
        }
        if (request.rankId() != null) {
            builder.and(review.rankId.eq(request.rankId()));
        }
        if (request.unitId() != null) {
            builder.and(review.unitId.eq(request.unitId()));
        }
        if (request.classification() != null) {
            builder.and(review.classification.eq(request.classification()));
        }
        if (request.status() != null) {
            builder.and(review.status.eq(request.status()));
        }

        return builder;
    }
}
