package com.navy.casualty.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navy.casualty.user.dto.UserCreateRequest;
import com.navy.casualty.user.dto.UserUpdateRoleRequest;
import com.navy.casualty.user.entity.User;
import com.navy.casualty.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserController 통합 테스트.
 * ADMIN 전용 사용자 관리 API 검증.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password1234"))
                .name("테스트사용자")
                .role("OPERATOR")
                .enabled(true)
                .accountLocked(false)
                .failedLoginCount(0)
                .build());
    }

    @Test
    @DisplayName("ADMIN - 사용자 목록 조회 -> 200")
    @WithMockUser(roles = "ADMIN")
    void listUsers_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("ADMIN - 사용자 생성 -> 201")
    @WithMockUser(roles = "ADMIN")
    void createUser_asAdmin_returns201() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "newuser", "password1234", "신규사용자", "VIEWER");

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.name").value("신규사용자"))
                .andExpect(jsonPath("$.data.role").value("VIEWER"));
    }

    @Test
    @DisplayName("ADMIN - 중복 username 생성 -> 409")
    @WithMockUser(roles = "ADMIN")
    void createUser_duplicate_returns409() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "testuser", "password1234", "중복사용자", "VIEWER");

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("ADMIN - 역할 변경 -> 200")
    @WithMockUser(roles = "ADMIN")
    void updateRole_asAdmin_returns200() throws Exception {
        UserUpdateRoleRequest request = new UserUpdateRoleRequest("MANAGER");

        mockMvc.perform(put("/api/admin/users/" + testUser.getId() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("ADMIN - 잠금 해제 -> 200")
    @WithMockUser(roles = "ADMIN")
    void unlockUser_asAdmin_returns200() throws Exception {
        mockMvc.perform(post("/api/admin/users/" + testUser.getId() + "/unlock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("ADMIN - 강제 로그아웃 -> 200")
    @WithMockUser(roles = "ADMIN")
    void forceLogout_asAdmin_returns200() throws Exception {
        mockMvc.perform(post("/api/admin/users/" + testUser.getId() + "/force-logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("ADMIN - 활성화 토글 -> 200")
    @WithMockUser(roles = "ADMIN")
    void toggleEnabled_asAdmin_returns200() throws Exception {
        mockMvc.perform(put("/api/admin/users/" + testUser.getId() + "/enabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("NON-ADMIN(OPERATOR) - 사용자 목록 접근 -> 403")
    @WithMockUser(roles = "OPERATOR")
    void listUsers_asOperator_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }
}
