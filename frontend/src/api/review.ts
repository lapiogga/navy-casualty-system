import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import { message } from 'antd';
import apiClient from './client';
import type {
  ReviewRecord,
  ReviewSearchParams,
  ReviewCreateForm,
  ReviewUpdateForm,
  ReviewHistoryRecord,
} from '../types/review';
import type { PageData } from '../types/dead';

// 코드 테이블 훅 re-export (dead.ts에서 정의된 것 재사용)
export { useRanks, useBranches, useUnits } from './dead';

interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  data: T;
}

/** 에러 메시지 추출 헬퍼 (타입 가드 사용) */
function getErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError<{ message: string }>(error)) {
    return error.response?.data?.message || fallback;
  }
  return fallback;
}

export const reviewKeys = {
  all: ['reviews'] as const,
  lists: () => [...reviewKeys.all, 'list'] as const,
  list: (params: ReviewSearchParams) => [...reviewKeys.lists(), params] as const,
  histories: (id: number) => [...reviewKeys.all, 'histories', id] as const,
};

export function useReviewList(params: ReviewSearchParams) {
  return useQuery({
    queryKey: reviewKeys.list(params),
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<PageData<ReviewRecord>>>('/reviews', { params });
      return res.data.data;
    },
  });
}

export function useCreateReview() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: ReviewCreateForm) =>
      apiClient.post('/reviews', data).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: reviewKeys.lists() });
      message.success('등록되었습니다');
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '등록에 실패했습니다'));
    },
  });
}

export function useUpdateReview() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: ReviewUpdateForm }) =>
      apiClient.put(`/reviews/${id}`, data).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: reviewKeys.lists() });
      message.success('수정되었습니다');
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '수정에 실패했습니다'));
    },
  });
}

export function useDeleteReview() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) =>
      apiClient.delete(`/reviews/${id}`, { data: { reason } }).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: reviewKeys.lists() });
      message.success('삭제되었습니다');
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '삭제에 실패했습니다'));
    },
  });
}

export function useUpdateReviewStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      apiClient.put(`/reviews/${id}/status`, null, { params: { status } }).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: reviewKeys.lists() });
      message.success('상태가 변경되었습니다');
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '상태 변경에 실패했습니다'));
    },
  });
}

// 이력 조회 (REVW-06)
export function useReviewHistories(reviewId: number | null) {
  return useQuery({
    queryKey: reviewKeys.histories(reviewId!),
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<ReviewHistoryRecord[]>>(
        `/reviews/${reviewId}/histories`,
      );
      return res.data.data;
    },
    enabled: reviewId !== null,
  });
}

/**
 * 전공사상심사 현황 Excel 다운로드 mutation 훅.
 * Blob 응답을 받아 브라우저 다운로드를 트리거한다.
 */
export function useExportReviewExcel() {
  return useMutation({
    mutationFn: async (params: ReviewSearchParams) => {
      const res = await apiClient.get('/reviews/excel', {
        params,
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.download = `review_list_${new Date().toISOString().slice(0, 10)}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    },
    onSuccess: () => {
      message.success('Excel 다운로드가 완료되었습니다');
    },
    onError: () => {
      message.error('Excel 다운로드에 실패했습니다');
    },
  });
}

// 보훈청 통보 일시 기록 (REVW-08)
export function useRecordNotification() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) =>
      apiClient.put(`/reviews/${id}/notify`).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: reviewKeys.lists() });
      message.success('보훈청 통보가 기록되었습니다');
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '보훈청 통보 기록에 실패했습니다'));
    },
  });
}
