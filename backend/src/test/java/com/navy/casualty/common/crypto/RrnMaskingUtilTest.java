package com.navy.casualty.common.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RrnMaskingUtil 단위 테스트.
 * D-04 3단계 마스킹 사양 검증.
 */
class RrnMaskingUtilTest {

    @Test
    @DisplayName("VIEWER 역할은 주민번호 전체를 마스킹한다")
    void viewerSeesFullMask() {
        assertThat(RrnMaskingUtil.mask("900101-1234567", "ROLE_VIEWER"))
                .isEqualTo("******-*******");
    }

    @Test
    @DisplayName("OPERATOR 역할은 앞 8자리만 보고 뒤 6자리는 마스킹한다")
    void operatorSeesPartialMask() {
        assertThat(RrnMaskingUtil.mask("900101-1234567", "ROLE_OPERATOR"))
                .isEqualTo("900101-1******");
    }

    @Test
    @DisplayName("MANAGER 역할은 주민번호 전체를 볼 수 있다")
    void managerSeesFullRrn() {
        assertThat(RrnMaskingUtil.mask("900101-1234567", "ROLE_MANAGER"))
                .isEqualTo("900101-1234567");
    }

    @Test
    @DisplayName("ADMIN 역할은 주민번호 전체를 볼 수 있다")
    void adminSeesFullRrn() {
        assertThat(RrnMaskingUtil.mask("900101-1234567", "ROLE_ADMIN"))
                .isEqualTo("900101-1234567");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("null 또는 빈 문자열 입력 시 빈 문자열을 반환한다")
    void nullOrEmptyReturnsEmpty(String input) {
        assertThat(RrnMaskingUtil.mask(input, "ROLE_VIEWER")).isEqualTo("");
    }
}
