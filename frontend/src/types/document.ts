/** 문서 유형 enum (백엔드 DocumentType과 동일) */
export enum DocumentType {
  DEAD_CERTIFICATE = 'DEAD_CERTIFICATE',
  WOUNDED_CERTIFICATE = 'WOUNDED_CERTIFICATE',
  REVIEW_RESULT = 'REVIEW_RESULT',
  DEATH_CONFIRMATION = 'DEATH_CONFIRMATION',
  DEAD_STATUS_REPORT = 'DEAD_STATUS_REPORT',
  WOUNDED_STATUS_REPORT = 'WOUNDED_STATUS_REPORT',
  ISSUE_LEDGER = 'ISSUE_LEDGER',
}

/** 문서 유형 한글 라벨 매핑 */
export const DOCUMENT_TYPE_LABELS: Record<DocumentType, string> = {
  [DocumentType.DEAD_CERTIFICATE]: '국가유공자 확인서(사망자)',
  [DocumentType.WOUNDED_CERTIFICATE]: '국가유공자 확인서(상이자)',
  [DocumentType.REVIEW_RESULT]: '전공사상심사결과서',
  [DocumentType.DEATH_CONFIRMATION]: '순직/사망확인서',
  [DocumentType.DEAD_STATUS_REPORT]: '사망자 현황 보고서',
  [DocumentType.WOUNDED_STATUS_REPORT]: '상이자 현황 보고서',
  [DocumentType.ISSUE_LEDGER]: '전사망자 확인증 발급대장',
};

/** 문서 생성 요청 파라미터 */
export interface GenerateDocumentParams {
  documentType: DocumentType;
  targetId: number;
  purpose: string;
}

/** 문서 발급 이력 응답 */
export interface DocumentIssueResponse {
  id: number;
  documentType: string;
  documentTypeName: string;
  targetTable: string;
  targetId: number;
  issuePurpose: string;
  issuedBy: string;
  issuedAt: string;
}

/** 문서 발급 이력 검색 파라미터 */
export interface DocumentIssueSearchParams {
  documentType?: DocumentType;
  issuedBy?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}
