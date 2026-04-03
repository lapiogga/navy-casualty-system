package com.navy.casualty.dead.entity;

import java.time.LocalDate;

import com.navy.casualty.common.crypto.PiiEncryptionConverter;
import com.navy.casualty.common.entity.BaseAuditEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * 사망자 엔티티. TB_DEAD 테이블에 매핑.
 */
@Entity
@Table(name = "TB_DEAD")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dead extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_number", nullable = false, unique = true, length = 20)
    private String serviceNumber;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Convert(converter = PiiEncryptionConverter.class)
    @Column(name = "ssn_encrypted", nullable = false, columnDefinition = "TEXT")
    private String ssnEncrypted;

    @Column(name = "ssn_hash", length = 64)
    private String ssnHash;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "rank_id")
    private Long rankId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "enlistment_date")
    private LocalDate enlistmentDate;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "death_type_id")
    private Long deathTypeId;

    @Column(name = "death_code_id")
    private Long deathCodeId;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "death_date", nullable = false)
    private LocalDate deathDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeadStatus status;

    @Builder
    private Dead(String serviceNumber, String name, String ssnEncrypted, String ssnHash,
                 LocalDate birthDate, Long rankId, Long branchId, Long unitId,
                 LocalDate enlistmentDate, String phone, Long deathTypeId, Long deathCodeId,
                 String address, LocalDate deathDate) {
        this.serviceNumber = serviceNumber;
        this.name = name;
        this.ssnEncrypted = ssnEncrypted;
        this.ssnHash = ssnHash;
        this.birthDate = birthDate;
        this.rankId = rankId;
        this.branchId = branchId;
        this.unitId = unitId;
        this.enlistmentDate = enlistmentDate;
        this.phone = phone;
        this.deathTypeId = deathTypeId;
        this.deathCodeId = deathCodeId;
        this.address = address;
        this.deathDate = deathDate;
        this.status = DeadStatus.REGISTERED;
    }

    /**
     * 상태를 전이한다. 허용되지 않은 전이 시 예외 발생.
     */
    public void updateStatus(DeadStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("상태 전이 불가: %s -> %s", this.status, newStatus));
        }
        this.status = newStatus;
    }
}
