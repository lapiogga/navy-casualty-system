package com.navy.casualty.wounded.entity;

import java.time.LocalDate;

import com.navy.casualty.common.crypto.PiiEncryptionConverter;
import com.navy.casualty.common.entity.BaseAuditEntity;
import com.navy.casualty.wounded.dto.WoundedUpdateRequest;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * 상이자 엔티티.
 * TB_WOUNDED 테이블에 매핑되며, 논리 삭제를 지원한다.
 */
@Entity
@Table(name = "TB_WOUNDED")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Wounded extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_number", nullable = false, unique = true, length = 20)
    private String serviceNumber;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "ssn_encrypted", nullable = false)
    @Convert(converter = PiiEncryptionConverter.class)
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

    @Column(length = 20)
    private String phone;

    private String address;

    @Column(name = "veterans_office_id")
    private Long veteransOfficeId;

    @Column(name = "disease_name", length = 200)
    private String diseaseName;

    @Enumerated(EnumType.STRING)
    @Column(name = "wound_type", nullable = false, length = 20)
    private WoundType woundType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private WoundedStatus status = WoundedStatus.REGISTERED;

    /**
     * 상태를 전이한다. 허용되지 않은 전이 시 IllegalStateException.
     */
    public void updateStatus(WoundedStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    this.status + "에서 " + newStatus + "로 전이할 수 없습니다");
        }
        this.status = newStatus;
    }

    /**
     * 상이자 정보를 수정한다. 군번은 수정 불가.
     */
    public void update(WoundedUpdateRequest request) {
        this.name = request.name();
        this.birthDate = request.birthDate();
        this.rankId = request.rankId();
        this.branchId = request.branchId();
        this.unitId = request.unitId();
        this.enlistmentDate = request.enlistmentDate();
        this.phone = request.phone();
        this.address = request.address();
        this.veteransOfficeId = request.veteransOfficeId();
        this.diseaseName = request.diseaseName();
        this.woundType = request.woundType();
    }

    /**
     * 주민번호 변경 시 암호화 값과 해시를 갱신한다.
     */
    public void updateSsn(String ssnPlain, String ssnHash) {
        this.ssnEncrypted = ssnPlain;
        this.ssnHash = ssnHash;
    }
}
