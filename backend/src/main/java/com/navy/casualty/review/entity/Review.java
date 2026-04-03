package com.navy.casualty.review.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.navy.casualty.common.crypto.PiiEncryptionConverter;
import com.navy.casualty.common.entity.BaseAuditEntity;
import com.navy.casualty.review.dto.ReviewUpdateRequest;
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
 * 전공사상심사 엔티티.
 * TB_REVIEW 테이블에 매핑되며, 논리 삭제를 지원한다.
 */
@Entity
@Table(name = "TB_REVIEW")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_round", nullable = false)
    private Integer reviewRound;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "service_number", nullable = false, length = 20)
    private String serviceNumber;

    @Column(name = "ssn_encrypted")
    @Convert(converter = PiiEncryptionConverter.class)
    private String ssnEncrypted;

    @Column(name = "ssn_hash", length = 64)
    private String ssnHash;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "rank_id")
    private Long rankId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "enlistment_date")
    private LocalDate enlistmentDate;

    @Column(name = "disease_name", length = 200)
    private String diseaseName;

    @Column(name = "unit_review_result", length = 20)
    private String unitReviewResult;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReviewClassification classification;

    @Column(name = "notification_date")
    private LocalDateTime notificationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.REGISTERED;

    /**
     * 심사 정보를 수정한다. 군번은 수정 불가.
     */
    public void update(ReviewUpdateRequest request) {
        this.reviewRound = request.reviewRound();
        this.reviewDate = request.reviewDate();
        this.name = request.name();
        this.birthDate = request.birthDate();
        this.rankId = request.rankId();
        this.branchId = request.branchId();
        this.unitId = request.unitId();
        this.enlistmentDate = request.enlistmentDate();
        this.diseaseName = request.diseaseName();
        this.unitReviewResult = request.unitReviewResult();
        this.classification = request.classification();
    }

    /**
     * 상태를 전이한다. 허용되지 않은 전이 시 IllegalStateException.
     */
    public void updateStatus(ReviewStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    this.status + "에서 " + newStatus + "로 전이할 수 없습니다");
        }
        this.status = newStatus;
    }

    /**
     * 주민번호 변경 시 암호화 값과 해시를 갱신한다.
     */
    public void updateSsn(String ssnPlain, String ssnHash) {
        this.ssnEncrypted = ssnPlain;
        this.ssnHash = ssnHash;
    }

    /**
     * 보훈청 통보 일시를 기록하고 상태를 NOTIFIED로 전이한다.
     */
    public void recordNotification() {
        this.notificationDate = LocalDateTime.now();
        updateStatus(ReviewStatus.NOTIFIED);
    }
}
