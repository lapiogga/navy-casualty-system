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
 * 사망구분 기호 코드 엔티티.
 */
@Entity
@Table(name = "TB_DEATH_CODE")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeathCode extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_symbol", nullable = false, unique = true, length = 10)
    private String codeSymbol;

    @Column(name = "code_name", nullable = false, length = 100)
    private String codeName;
}
