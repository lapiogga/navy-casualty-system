package com.navy.casualty.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 감사 로그 자동 기록 어노테이션.
 * 서비스 메서드에 이 어노테이션을 붙이면 AOP를 통해
 * TB_AUDIT_LOG에 자동으로 감사 로그가 기록된다.
 *
 * D-08: LIST 조회에는 이 어노테이션을 붙이지 않아 기록을 생략한다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 감사 액션 유형.
     * VIEW, CREATE, UPDATE, DELETE, PRINT, EXPORT 중 하나.
     */
    String action();

    /**
     * 대상 테이블명 (예: "TB_DEAD", "TB_WOUNDED").
     */
    String targetTable() default "";
}
