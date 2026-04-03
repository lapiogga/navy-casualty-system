package com.navy.casualty.statistics.repository;

import java.util.List;

import com.navy.casualty.code.entity.QBranchCode;
import com.navy.casualty.code.entity.QUnitCode;
import com.navy.casualty.dead.entity.Dead;
import com.navy.casualty.dead.entity.QDead;
import com.navy.casualty.statistics.dto.BranchStatResponse;
import com.navy.casualty.statistics.dto.MonthlyStatResponse;
import com.navy.casualty.statistics.dto.UnitStatResponse;
import com.navy.casualty.statistics.dto.YearlyStatResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * QueryDSL 기반 통계 집계 쿼리 구현.
 * 모든 쿼리에 dead.deletedAt.isNull() 명시 (Projections 사용 시 @SQLRestriction 미적용 방지).
 */
@Repository
@RequiredArgsConstructor
public class StatisticsRepositoryImpl implements StatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BranchStatResponse> getByBranch() {
        QDead dead = QDead.dead;
        QBranchCode branch = QBranchCode.branchCode;

        return queryFactory
                .select(Projections.constructor(BranchStatResponse.class,
                        new CaseBuilder()
                                .when(branch.branchName.isNull())
                                .then("미분류")
                                .otherwise(branch.branchName),
                        dead.count()))
                .from(dead)
                .leftJoin(branch).on(dead.branchId.eq(branch.id))
                .where(dead.deletedAt.isNull())
                .groupBy(branch.id, branch.branchName)
                .orderBy(dead.count().desc())
                .fetch();
    }

    @Override
    public List<MonthlyStatResponse> getByMonth() {
        QDead dead = QDead.dead;

        NumberExpression<Integer> yearExpr = Expressions.numberTemplate(
                Integer.class, "EXTRACT(YEAR FROM {0})", dead.deathDate);
        NumberExpression<Integer> monthExpr = Expressions.numberTemplate(
                Integer.class, "EXTRACT(MONTH FROM {0})", dead.deathDate);

        return queryFactory
                .select(Projections.constructor(MonthlyStatResponse.class,
                        yearExpr, monthExpr, dead.count()))
                .from(dead)
                .where(dead.deletedAt.isNull())
                .groupBy(yearExpr, monthExpr)
                .orderBy(yearExpr.desc(), monthExpr.desc())
                .fetch();
    }

    @Override
    public List<YearlyStatResponse> getByYear() {
        QDead dead = QDead.dead;

        NumberExpression<Integer> yearExpr = Expressions.numberTemplate(
                Integer.class, "EXTRACT(YEAR FROM {0})", dead.deathDate);

        return queryFactory
                .select(Projections.constructor(YearlyStatResponse.class,
                        yearExpr, dead.count()))
                .from(dead)
                .where(dead.deletedAt.isNull())
                .groupBy(yearExpr)
                .orderBy(yearExpr.desc())
                .fetch();
    }

    @Override
    public List<UnitStatResponse> getByUnit() {
        QDead dead = QDead.dead;
        QUnitCode unit = QUnitCode.unitCode;

        return queryFactory
                .select(Projections.constructor(UnitStatResponse.class,
                        new CaseBuilder()
                                .when(unit.unitName.isNull())
                                .then("미분류")
                                .otherwise(unit.unitName),
                        dead.count()))
                .from(dead)
                .leftJoin(unit).on(dead.unitId.eq(unit.id))
                .where(dead.deletedAt.isNull())
                .groupBy(unit.id, unit.unitName)
                .orderBy(dead.count().desc())
                .fetch();
    }

    @Override
    public List<Dead> getRosterByUnit(Long unitId) {
        QDead dead = QDead.dead;

        return queryFactory
                .selectFrom(dead)
                .where(dead.deletedAt.isNull()
                        .and(dead.unitId.eq(unitId)))
                .orderBy(dead.id.desc())
                .fetch();
    }

    @Override
    public List<Dead> getRosterAll() {
        QDead dead = QDead.dead;

        return queryFactory
                .selectFrom(dead)
                .where(dead.deletedAt.isNull())
                .orderBy(dead.id.desc())
                .fetch();
    }
}
