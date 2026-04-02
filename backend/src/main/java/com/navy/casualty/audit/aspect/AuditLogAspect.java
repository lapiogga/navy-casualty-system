package com.navy.casualty.audit.aspect;

import com.navy.casualty.audit.annotation.AuditLog;
import com.navy.casualty.audit.entity.AuditLogEntry;
import com.navy.casualty.audit.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP 감사 로그 Aspect.
 * @AuditLog 어노테이션이 붙은 메서드 호출 시 자동으로
 * TB_AUDIT_LOG에 감사 로그를 기록한다.
 *
 * D-08: LIST 조회는 @AuditLog를 붙이지 않으므로 기록되지 않는다.
 * D-09: detail은 한줄 요약 텍스트 (JSON diff 아님).
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(auditLog)")
    public Object logAudit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        String userId = extractUserId();
        String ipAddress = extractIpAddress();
        Long targetId = extractTargetId(joinPoint.getArgs());

        try {
            Object result = joinPoint.proceed();

            String detail = buildSuccessDetail(auditLog.action(), auditLog.targetTable(), targetId);
            saveLog(userId, auditLog.action(), auditLog.targetTable(), targetId, detail, ipAddress);

            return result;
        } catch (Throwable e) {
            String detail = "실패: " + e.getMessage();
            saveLog(userId, auditLog.action(), auditLog.targetTable(), targetId, detail, ipAddress);
            throw e;
        }
    }

    private void saveLog(String userId, String action, String targetTable,
                         Long targetId, String detail, String ipAddress) {
        try {
            AuditLogEntry entry = AuditLogEntry.builder()
                    .userId(userId)
                    .action(action)
                    .targetTable(targetTable)
                    .targetId(targetId)
                    .detail(detail)
                    .ipAddress(ipAddress)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            // 감사 로그 저장 실패가 비즈니스 로직을 방해하지 않도록 로깅만 수행
            log.error("감사 로그 저장 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * SecurityContextHolder에서 현재 사용자 ID를 추출한다.
     */
    private String extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "ANONYMOUS";
    }

    /**
     * HttpServletRequest에서 클라이언트 IP를 추출한다.
     */
    private String extractIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String forwarded = request.getHeader("X-Forwarded-For");
                if (forwarded != null && !forwarded.isBlank()) {
                    return forwarded.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("IP 주소 추출 실패: {}", e.getMessage());
        }
        return "UNKNOWN";
    }

    /**
     * 메서드 인자에서 Long/long 타입 첫 번째 값을 targetId로 추출한다.
     */
    private Long extractTargetId(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof Long longValue) {
                return longValue;
            }
        }
        return null;
    }

    /**
     * D-09: 성공 시 한줄 요약 detail 생성.
     */
    private String buildSuccessDetail(String action, String targetTable, Long targetId) {
        String table = (targetTable != null && !targetTable.isBlank())
                ? targetTable.replace("TB_", "") : "대상";
        String id = (targetId != null) ? " ID " + targetId : "";

        return switch (action) {
            case "CREATE" -> table + id + " 등록";
            case "UPDATE" -> table + id + " 수정";
            case "DELETE" -> table + id + " 삭제";
            case "VIEW" -> table + id + " 상세 조회";
            case "PRINT" -> table + id + " 출력";
            case "EXPORT" -> table + id + " 내보내기";
            default -> table + id + " " + action;
        };
    }
}
