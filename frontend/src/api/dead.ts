import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import { message } from 'antd';
import apiClient from './client';
import type {
  DeadRecord,
  DeadSearchParams,
  DeadCreateForm,
  DeadUpdateForm,
  PageData,
  RankCode,
  BranchCode,
  DeathType,
  DeathCode,
  UnitCode,
} from '../types/dead';

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

export const deadKeys = {
  all: ['dead'] as const,
  lists: () => [...deadKeys.all, 'list'] as const,
  list: (params: DeadSearchParams) => [...deadKeys.lists(), params] as const,
};

export function useDeadList(params: DeadSearchParams) {
  return useQuery({
    queryKey: deadKeys.list(params),
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<PageData<DeadRecord>>>('/dead', { params });
      return res.data.data;
    },
  });
}

export function useCreateDead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: DeadCreateForm) =>
      apiClient.post('/dead', data).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: deadKeys.lists() });
      message.success('등록되었습니다');
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '등록에 실패했습니다'));
    },
  });
}

export function useUpdateDead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: DeadUpdateForm }) =>
      apiClient.put(`/dead/${id}`, data).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: deadKeys.lists() });
      message.success('수정되었습니다');
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '수정에 실패했습니다'));
    },
  });
}

export function useDeleteDead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) =>
      apiClient.delete(`/dead/${id}`, { data: { reason } }).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: deadKeys.lists() });
      message.success('삭제되었습니다');
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '삭제에 실패했습니다'));
    },
  });
}

export function useUpdateDeadStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      apiClient.put(`/dead/${id}/status`, null, { params: { status } }).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: deadKeys.lists() });
      message.success('상태가 변경되었습니다');
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '상태 변경에 실패했습니다'));
    },
  });
}

/**
 * 사망자 현황 Excel 다운로드 mutation 훅.
 * Blob 응답을 받아 브라우저 다운로드를 트리거한다.
 */
export function useExportDeadExcel() {
  return useMutation({
    mutationFn: async (params: DeadSearchParams) => {
      const res = await apiClient.get('/dead/excel', {
        params,
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.download = `dead_list_${new Date().toISOString().slice(0, 10)}.xlsx`;
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

// 코드 테이블 훅
export function useRanks() {
  return useQuery({
    queryKey: ['codes', 'ranks'],
    queryFn: () =>
      apiClient.get<ApiResponse<RankCode[]>>('/codes/ranks').then((r) => r.data.data),
  });
}

export function useBranches() {
  return useQuery({
    queryKey: ['codes', 'branches'],
    queryFn: () =>
      apiClient.get<ApiResponse<BranchCode[]>>('/codes/branches').then((r) => r.data.data),
  });
}

export function useDeathTypes() {
  return useQuery({
    queryKey: ['codes', 'death-types'],
    queryFn: () =>
      apiClient.get<ApiResponse<DeathType[]>>('/codes/death-types').then((r) => r.data.data),
  });
}

export function useDeathCodes() {
  return useQuery({
    queryKey: ['codes', 'death-codes'],
    queryFn: () =>
      apiClient.get<ApiResponse<DeathCode[]>>('/codes/death-codes').then((r) => r.data.data),
  });
}

export function useUnits() {
  return useQuery({
    queryKey: ['codes', 'units'],
    queryFn: () =>
      apiClient.get<ApiResponse<UnitCode[]>>('/codes/units').then((r) => r.data.data),
  });
}
