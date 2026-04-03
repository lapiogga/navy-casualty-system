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
 * 보훈청/보훈지청 코드 엔티티.
 */
@Entity
@Table(name = "TB_VETERANS_OFFICE")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VeteransOffice extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "office_name", nullable = false, length = 100)
    private String officeName;

    @Column(name = "office_type", nullable = false, length = 20)
    private String officeType;
}
