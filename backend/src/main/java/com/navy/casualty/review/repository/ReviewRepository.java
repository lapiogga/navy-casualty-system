package com.navy.casualty.review.repository;

import java.util.Optional;

import com.navy.casualty.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 전공사상심사 리포지토리.
 */
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

    boolean existsByServiceNumberAndReviewRound(String serviceNumber, int reviewRound);

    boolean existsBySsnHash(String ssnHash);

    Optional<Review> findByServiceNumber(String serviceNumber);
}
