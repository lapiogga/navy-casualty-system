package com.navy.casualty.wounded.repository;

import com.navy.casualty.wounded.entity.Wounded;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 상이자 리포지토리.
 */
public interface WoundedRepository extends JpaRepository<Wounded, Long>, WoundedRepositoryCustom {

    boolean existsByServiceNumber(String serviceNumber);

    boolean existsBySsnHash(String ssnHash);
}
