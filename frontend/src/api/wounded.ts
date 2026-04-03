import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import { message } from 'antd';
import apiClient from './client';
import type { WoundedRecord, WoundedSearchParams, PageData } from '../types/wounded';

interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  data: T;
}

/** 에러 메시지 추출 헬퍼 (타입 가드 사용, any 없음) */
function getErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError<{ message: string }>(error)) {
    return error.response?.data?.message || fallback;
  }
  return fallback;
}

export const woundedKeys = {
  all: ['wounded'] as const,
  lists: () => [...woundedKeys.all, 'list'] as const,
  list: (params: WoundedSearchParams) => [...woundedKeys.lists(), params] as const,
};

export function useWoundedList(params: WoundedSearchParams) {
  return useQuery({
    queryKey: woundedKeys.list(params),
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<PageData<WoundedRecord>>>('/wounded', { params });
      return res.data.data;
    },
  });
}

/**
 * 상이자 현황 Excel 다운로드 mutation 훅.
 * Blob 응답을 받아 브라우저 다운로드를 트리거한다.
 */
export function useExportWoundedExcel() {
  return useMutation({
    mutationFn: async (params: WoundedSearchParams) => {
      const res = await apiClient.get('/wounded/excel', {
        params,
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.download = `wounded_list_${new Date().toISOString().slice(0, 10)}.xlsx`;
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

export function useCreateWounded() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Record<string, unknown>) =>
      apiClient.post('/wounded', data).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: woundedKeys.lists() });
      message.success('등록되었습니다');
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '등록에 실패했습니다'));
    },
  });
}

export function useUpdateWounded() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Record<string, unknown> }) =>
      apiClient.put(`/wounded/${id}`, data).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: woundedKeys.lists() });
      message.success('수정되었습니다');
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '수정에 실패했습니다'));
    },
  });
}

export function useDeleteWounded() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) =>
      apiClient.delete(`/wounded/${id}`, { data: { reason } }).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: woundedKeys.lists() });
      message.success('삭제되었습니다');
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '삭제에 실패했습니다'));
    },
  });
}

export function useUpdateWoundedStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      apiClient.put(`/wounded/${id}/status`, null, { params: { status } }).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: woundedKeys.lists() });
      message.success('상태가 변경되었습니다');
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '상태 변경에 실패했습니다'));
    },
  });
}
