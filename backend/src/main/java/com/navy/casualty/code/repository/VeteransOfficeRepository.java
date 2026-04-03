package com.navy.casualty.code.repository;

import com.navy.casualty.code.entity.VeteransOffice;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 보훈청 코드 리포지토리.
 */
public interface VeteransOfficeRepository extends JpaRepository<VeteransOffice, Long> {
}
