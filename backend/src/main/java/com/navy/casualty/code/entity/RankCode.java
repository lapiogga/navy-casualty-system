package com.navy.casualty.code.entity;

import com.navy.casualty.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * 계급 코드 엔티티.
 */
@Entity
@Table(name = "TB_RANK_CODE")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankCode extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rank_name", nullable = false, length = 50)
    private String rankName;

    @Column(name = "rank_group", nullable = false, length = 20)
    private String rankGroup;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
