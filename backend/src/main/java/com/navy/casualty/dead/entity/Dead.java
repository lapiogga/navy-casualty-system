package com.navy.casualty.dead.entity;

import java.time.LocalDate;

import com.navy.casualty.common.crypto.PiiEncryptionConverter;
import com.navy.casualty.common.entity.BaseAuditEntity;
import com.navy.casualty.dead.dto.DeadUpdateRequest;
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
 * 사망자 엔티티.
 * TB_DEAD 테이블에 매핑되며, 논리 삭제를 지원한다.
 */
@Entity
@Table(name = "TB_DEAD")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Dead extends BaseAuditEntity {

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

    @Column(name = "death_type_id")
    private Long deathTypeId;

    @Column(name = "death_code_id")
    private Long deathCodeId;

    private String address;

    @Column(name = "death_date", nullable = false)
    private LocalDate deathDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DeadStatus status = DeadStatus.REGISTERED;

    /**
     * 상태를 전이한다. 허용되지 않은 전이 시 IllegalStateException.
     */
    public void updateStatus(DeadStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    this.status + "에서 " + newStatus + "로 전이할 수 없습니다");
        }
        this.status = newStatus;
    }

    /**
     * 사망자 정보를 수정한다. 군번은 수정 불가.
     */
    public void update(DeadUpdateRequest request) {
        this.name = request.name();
        this.birthDate = request.birthDate();
        this.rankId = request.rankId();
        this.branchId = request.branchId();
        this.unitId = request.unitId();
        this.enlistmentDate = request.enlistmentDate();
        this.phone = request.phone();
        this.deathTypeId = request.deathTypeId();
        this.deathCodeId = request.deathCodeId();
        this.address = request.address();
        this.deathDate = request.deathDate();
    }

    /**
     * 사망구분을 변경한다 (전공사상심사 결과 자동반영용).
     */
    public void updateDeathType(Long deathTypeId) {
        this.deathTypeId = deathTypeId;
    }

    /**
     * 주민번호 변경 시 암호화 값과 해시를 갱신한다.
     */
    public void updateSsn(String ssnPlain, String ssnHash) {
        this.ssnEncrypted = ssnPlain;
        this.ssnHash = ssnHash;
    }
}
