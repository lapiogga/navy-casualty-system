package com.navy.casualty.auth.controller;

import com.navy.casualty.auth.dto.ChangePasswordRequest;
import com.navy.casualty.auth.dto.LoginRequest;
import com.navy.casualty.auth.dto.LoginResponse;
import com.navy.casualty.auth.service.AuthService;
import com.navy.casualty.common.dto.ApiResponse;
import com.navy.casualty.security.CustomUserDetails;
import com.navy.casualty.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 REST API 컨트롤러.
 * 로그인, 로그아웃, 현재 사용자 조회를 담당한다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String LOGIN_ERROR_MESSAGE = "사용자 ID 또는 비밀번호가 올바르지 않습니다";

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    /**
     * 로그인 처리.
     * 성공 시 세션에 SecurityContext를 저장하고, 실패 시 실패 횟수를 증가시킨다.
     * D-02: 잠금/실패 구분 없이 동일한 에러 메시지를 반환한다.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(), request.password()));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);

            authService.resetFailCount(request.username());

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return ResponseEntity.ok(ApiResponse.ok(LoginResponse.from(userDetails)));

        } catch (AuthenticationException e) {
            authService.incrementFailCount(request.username());
            return ResponseEntity
                    .status(401)
                    .body(ApiResponse.error(401, LOGIN_ERROR_MESSAGE));
        }
    }

    /**
     * 로그아웃 처리.
     * 세션을 무효화하고 SecurityContext를 초기화한다.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 비밀번호 변경 처리.
     * 현재 비밀번호를 검증한 후 새 비밀번호로 변경한다.
     */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        CustomUserDetails user = SecurityUtils.getCurrentUser();
        authService.changePassword(user.getId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.ok(null, "비밀번호가 변경되었습니다"));
    }

    /**
     * 현재 인증된 사용자 정보를 반환한다.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse>> me() {
        CustomUserDetails user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.ok(LoginResponse.from(user)));
    }
}
