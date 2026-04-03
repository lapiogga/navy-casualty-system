package com.navy.casualty.review.repository;

import java.util.List;

import com.navy.casualty.review.dto.ReviewSearchRequest;
import com.navy.casualty.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 전공사상심사 동적 검색 커스텀 리포지토리.
 */
public interface ReviewRepositoryCustom {

    Page<Review> search(ReviewSearchRequest request, Pageable pageable);

    List<Review> searchAll(ReviewSearchRequest request);
}
