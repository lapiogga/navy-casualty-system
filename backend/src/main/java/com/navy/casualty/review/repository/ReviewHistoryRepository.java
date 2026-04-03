package com.navy.casualty.review.repository;

import java.util.List;

import com.navy.casualty.review.entity.ReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 전공사상심사 이력 리포지토리.
 */
public interface ReviewHistoryRepository extends JpaRepository<ReviewHistory, Long> {

    List<ReviewHistory> findByReviewIdOrderByChangedAtDesc(Long reviewId);
}
