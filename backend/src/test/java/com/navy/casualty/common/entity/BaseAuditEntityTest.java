package com.navy.casualty.common.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseAuditEntityTest {

    /**
     * 테스트용 구현체 (추상 클래스이므로 직접 인스턴스화 불가).
     */
    static class TestEntity extends BaseAuditEntity {
    }

    @Test
    @DisplayName("초기 상태 - isDeleted()가 false")
    void initialState_isNotDeleted() {
        TestEntity entity = new TestEntity();

        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.getDeletedBy()).isNull();
        assertThat(entity.getDeleteReason()).isNull();
    }

    @Test
    @DisplayName("softDelete() 호출 후 - isDeleted()가 true, 삭제 정보 설정됨")
    void softDelete_setsDeletedFields() {
        TestEntity entity = new TestEntity();

        entity.softDelete("admin", "테스트 삭제 사유");

        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getDeletedAt()).isNotNull();
        assertThat(entity.getDeletedBy()).isEqualTo("admin");
        assertThat(entity.getDeleteReason()).isEqualTo("테스트 삭제 사유");
    }
}
