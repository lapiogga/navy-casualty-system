import { useQuery, useMutation } from '@tanstack/react-query';
import axios from 'axios';
import { message } from 'antd';
import apiClient from './client';
import type { DocumentIssueResponse, DocumentIssueSearchParams, GenerateDocumentParams } from '../types/document';
import type { PageData } from '../types/dead';

interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  data: T;
}

/** 에러 메시지 추출 헬퍼 */
function getErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError<{ message: string }>(error)) {
    return error.response?.data?.message || fallback;
  }
  return fallback;
}

export const documentKeys = {
  all: ['document'] as const,
  issues: () => [...documentKeys.all, 'issues'] as const,
  issueList: (params: DocumentIssueSearchParams) => [...documentKeys.issues(), params] as const,
};

/**
 * 문서 생성 mutation.
 * POST /documents/{type}/generate?targetId={id}, body: {purpose}
 * 응답: PDF Blob
 */
export function useGenerateDocument() {
  return useMutation({
    mutationFn: async (params: GenerateDocumentParams) => {
      const res = await apiClient.post<Blob>(
        `/documents/${params.documentType}/generate`,
        { purpose: params.purpose },
        {
          params: { targetId: params.targetId },
          responseType: 'blob',
        },
      );
      return res.data;
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '문서 생성에 실패했습니다'));
    },
  });
}

/**
 * 문서 발급 이력 조회 query.
 * GET /documents/issues
 */
export function useDocumentIssues(params: DocumentIssueSearchParams) {
  return useQuery({
    queryKey: documentKeys.issueList(params),
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<PageData<DocumentIssueResponse>>>('/documents/issues', { params });
      return res.data.data;
    },
  });
}
