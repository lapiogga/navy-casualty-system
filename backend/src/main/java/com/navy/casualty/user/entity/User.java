package com.navy.casualty.user.entity;

import java.time.LocalDateTime;

import com.navy.casualty.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 사용자 엔티티.
 * TB_USER 테이블에 매핑되며, 논리 삭제를 지원한다.
 */
@Entity
@Table(name = "TB_USER")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "VIEWER";

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private boolean accountLocked = false;

    @Column(name = "failed_login_count", nullable = false)
    @Builder.Default
    private int failedLoginCount = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_changed", nullable = false)
    @Builder.Default
    private boolean passwordChanged = false;

    /**
     * 로그인 실패 횟수를 1 증가시킨다.
     * 5회 이상이면 계정을 잠근다.
     */
    public void incrementFailCount() {
        this.failedLoginCount++;
        if (this.failedLoginCount >= 5) {
            this.accountLocked = true;
        }
    }

    /**
     * 로그인 성공 시 실패 횟수를 초기화하고 마지막 로그인 시각을 기록한다.
     */
    public void resetFailCount() {
        this.failedLoginCount = 0;
        this.accountLocked = false;
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 관리자에 의한 계정 잠금 해제.
     */
    public void unlock() {
        this.accountLocked = false;
        this.failedLoginCount = 0;
    }

    /**
     * 비밀번호를 변경하고 변경 완료 플래그를 설정한다.
     */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.passwordChanged = true;
    }

    /**
     * 역할을 변경한다.
     */
    public void changeRole(String role) {
        this.role = role;
    }

    /**
     * 활성/비활성 상태를 토글한다.
     */
    public void toggleEnabled() {
        this.enabled = !this.enabled;
    }
}
