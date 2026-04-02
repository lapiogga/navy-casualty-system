package com.navy.casualty.common.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 감사 기본 엔티티.
 * 모든 엔티티가 상속하여 생성/수정/삭제 이력을 자동 추적한다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    @Column(name = "delete_reason", length = 500)
    private String deleteReason;

    /**
     * 논리 삭제를 수행한다.
     *
     * @param deletedBy   삭제 수행자
     * @param reason      삭제 사유
     */
    public void softDelete(String deletedBy, String reason) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.deleteReason = reason;
    }

    /**
     * 논리 삭제 여부를 반환한다.
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
