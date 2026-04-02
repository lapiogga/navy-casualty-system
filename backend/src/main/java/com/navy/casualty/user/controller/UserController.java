package com.navy.casualty.user.controller;

import java.util.List;

import com.navy.casualty.auth.service.AuthService;
import com.navy.casualty.common.dto.ApiResponse;
import com.navy.casualty.user.dto.UserCreateRequest;
import com.navy.casualty.user.dto.UserResponse;
import com.navy.casualty.user.dto.UserUpdateRoleRequest;
import com.navy.casualty.user.entity.User;
import com.navy.casualty.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ADMIN 전용 사용자 관리 컨트롤러.
 * 사용자 CRUD, 잠금 해제, 강제 로그아웃, 활성화 토글을 제공한다.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final SessionRegistry sessionRegistry;

    /**
     * 전체 사용자 목록을 조회한다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> listUsers() {
        List<UserResponse> users = userService.findAll().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    /**
     * 새 사용자를 생성한다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        try {
            User created = userService.createUser(
                    request.username(), request.password(),
                    request.name(), request.role());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(UserResponse.from(created)));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(409, "이미 존재하는 사용자 ID입니다"));
        }
    }

    /**
     * 사용자 역할을 변경한다.
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<Void>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRoleRequest request) {
        userService.updateRole(id, request.role());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 계정 잠금을 해제한다.
     */
    @PostMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockAccount(@PathVariable Long id) {
        authService.unlockAccount(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 강제 로그아웃 (AUTH-07).
     * SessionRegistry에서 해당 사용자의 모든 세션을 만료시킨다.
     */
    @PostMapping("/{id}/force-logout")
    public ResponseEntity<ApiResponse<Void>> forceLogout(@PathVariable Long id) {
        User user = userService.findById(id);
        List<SessionInformation> sessions = sessionRegistry
                .getAllSessions(user.getUsername(), false);
        sessions.forEach(SessionInformation::expireNow);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 사용자 활성/비활성 상태를 토글한다.
     */
    @PutMapping("/{id}/enabled")
    public ResponseEntity<ApiResponse<Void>> toggleEnabled(@PathVariable Long id) {
        userService.toggleEnabled(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
