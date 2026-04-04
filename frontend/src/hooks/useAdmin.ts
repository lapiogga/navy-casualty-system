import { useMutation } from '@tanstack/react-query';
import axios from 'axios';
import { message } from 'antd';
import apiClient from '../api/client';

interface ImportError {
  rowNumber: number;
  column: string;
  reason: string;
}

interface ImportResult {
  totalRows: number;
  successRows: number;
  errorRows: number;
  errors: ImportError[];
}

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

/**
 * Excel 임포트 훅.
 * POST /api/admin/import/{type} 엔드포인트를 호출한다.
 */
export function useImportExcel() {
  return useMutation({
    mutationFn: async ({ type, file }: { type: string; file: File }) => {
      const formData = new FormData();
      formData.append('file', file);
      const res = await apiClient.post<ApiResponse<ImportResult>>(
        `/admin/import/${type}`,
        formData,
        { headers: { 'Content-Type': 'multipart/form-data' } },
      );
      return res.data.data;
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '임포트 실패'));
    },
  });
}

/**
 * 감사 보고서 다운로드 훅.
 * GET /api/admin/audit-report 엔드포인트를 호출하여 PDF blob을 반환한다.
 */
export function useAuditReport() {
  return useMutation({
    mutationFn: async ({ year, month }: { year: number; month: number }) => {
      const res = await apiClient.get('/admin/audit-report', {
        params: { year, month },
        responseType: 'blob',
      });
      return res.data as Blob;
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '보고서 생성 실패'));
    },
  });
}
