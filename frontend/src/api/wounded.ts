import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import { message } from 'antd';
import apiClient from './client';
import type {
  WoundedRecord,
  WoundedSearchParams,
  WoundedCreateForm,
  WoundedUpdateForm,
  VeteransOffice,
} from '../types/wounded';
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

export function useCreateWounded() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: WoundedCreateForm) =>
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
    mutationFn: ({ id, data }: { id: number; data: WoundedUpdateForm }) =>
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

/** 보훈청 코드 훅 (상이자 전용) */
export function useVeteransOffices() {
  return useQuery({
    queryKey: ['codes', 'veterans-offices'],
    queryFn: () =>
      apiClient.get<ApiResponse<VeteransOffice[]>>('/codes/veterans-offices').then((r) => r.data.data),
  });
}
