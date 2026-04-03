package com.navy.casualty.review.entity;

import lombok.Getter;

/**
 * 전공사상심사 분류 enum.
 * 전공상(COMBAT_WOUND), 공상(DUTY_WOUND), 기각(REJECTED), 보류(DEFERRED).
 */
@Getter
public enum ReviewClassification {

    COMBAT_WOUND("전공상"),
    DUTY_WOUND("공상"),
    REJECTED("기각"),
    DEFERRED("보류");

    private final String label;

    ReviewClassification(String label) {
        this.label = label;
    }
}
