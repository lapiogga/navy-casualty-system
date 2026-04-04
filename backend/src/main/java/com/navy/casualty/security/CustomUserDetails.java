package com.navy.casualty.security;

import java.util.Collection;
import java.util.List;

import com.navy.casualty.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security UserDetails 구현체.
 * User 엔티티를 인증 컨텍스트에서 사용할 수 있도록 래핑한다.
 */
@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String name;
    private final String role;
    private final boolean enabled;
    private final boolean accountLocked;
    private final boolean passwordChanged;

    /**
     * User 엔티티로부터 CustomUserDetails를 생성한다.
     */
    public static CustomUserDetails from(User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getName(),
                user.getRole(),
                user.isEnabled(),
                user.isAccountLocked(),
                user.isPasswordChanged()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
