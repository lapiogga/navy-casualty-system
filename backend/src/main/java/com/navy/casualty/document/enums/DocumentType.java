package com.navy.casualty.document.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 문서 유형 enum (7종).
 * templateName: .jrxml 파일명 (확장자 제외)
 * fileName: 다운로드 시 파일명
 * targetTable: 대상 테이블명
 */
@Getter
@RequiredArgsConstructor
public enum DocumentType {

    DEAD_CERTIFICATE("dead_certificate", "국가유공자_확인서_사망자", "TB_DEAD"),
    WOUNDED_CERTIFICATE("wounded_certificate", "국가유공자_확인서_상이자", "TB_WOUNDED"),
    REVIEW_RESULT("review_result", "전공사상심사결과서", "TB_REVIEW"),
    DEATH_CONFIRMATION("death_confirmation", "순직사망확인서", "TB_DEAD"),
    DEAD_STATUS_REPORT("dead_status_report", "사망자현황보고서", "TB_DEAD"),
    WOUNDED_STATUS_REPORT("wounded_status_report", "상이자현황보고서", "TB_WOUNDED"),
    ISSUE_LEDGER("issue_ledger", "발급대장", "TB_DOCUMENT_ISSUE");

    private final String templateName;
    private final String fileName;
    private final String targetTable;
}
