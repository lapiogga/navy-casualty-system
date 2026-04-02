package com.navy.casualty.user.repository;

import java.util.Optional;

import com.navy.casualty.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 리포지토리.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
