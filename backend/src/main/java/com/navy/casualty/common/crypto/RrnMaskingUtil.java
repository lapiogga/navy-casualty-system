package com.navy.casualty.common.crypto;

/**
 * 주민등록번호 역할별 3단계 마스킹 유틸리티.
 * D-04 사양에 따라 역할별로 다른 수준의 마스킹을 적용한다.
 *
 * - ROLE_VIEWER: 전체 마스킹 (******-*******)
 * - ROLE_OPERATOR: 앞 8자리 노출, 뒤 6자리 마스킹 (900101-1******)
 * - ROLE_MANAGER / ROLE_ADMIN: 전체 노출
 */
public final class RrnMaskingUtil {

    private RrnMaskingUtil() {
        // 유틸리티 클래스 - 인스턴스 생성 방지
    }

    /**
     * 역할에 따라 주민등록번호를 마스킹한다.
     *
     * @param rrn  주민등록번호 (예: "900101-1234567")
     * @param role 사용자 역할 (예: "ROLE_VIEWER", "ROLE_OPERATOR", "ROLE_MANAGER", "ROLE_ADMIN")
     * @return 마스킹된 주민등록번호. null 또는 빈 입력 시 빈 문자열 반환.
     */
    public static String mask(String rrn, String role) {
        if (rrn == null || rrn.isBlank()) {
            return "";
        }

        return switch (role) {
            case "ROLE_VIEWER" -> "******-*******";
            case "ROLE_OPERATOR" -> rrn.substring(0, 8) + "******";
            default -> rrn; // ROLE_MANAGER, ROLE_ADMIN
        };
    }
}
