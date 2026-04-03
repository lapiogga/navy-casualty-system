import { useQuery, useMutation } from '@tanstack/react-query';
import { message } from 'antd';
import apiClient from './client';
import type {
  BranchStatResponse,
  MonthlyStatResponse,
  YearlyStatResponse,
  UnitStatResponse,
  DeadRosterResponse,
} from '../types/statistics';

interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  data: T;
}

/** Excel Blob 다운로드 헬퍼 */
function downloadBlob(data: BlobPart, filename: string) {
  const blob = new Blob([data]);
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

// Query keys
export const statisticsKeys = {
  all: ['statistics'] as const,
  branch: () => [...statisticsKeys.all, 'branch'] as const,
  monthly: () => [...statisticsKeys.all, 'monthly'] as const,
  yearly: () => [...statisticsKeys.all, 'yearly'] as const,
  unit: () => [...statisticsKeys.all, 'unit'] as const,
  rosterUnit: (unitId?: number) => [...statisticsKeys.all, 'roster-unit', unitId] as const,
  rosterAll: () => [...statisticsKeys.all, 'roster-all'] as const,
};

// ─── 조회 훅 6종 ───

export function useBranchStat() {
  return useQuery({
    queryKey: statisticsKeys.branch(),
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<BranchStatResponse[]>>('/statistics/branch');
      return res.data.data;
    },
  });
}

export function useMonthlyStat() {
  return useQuery({
    queryKey: statisticsKeys.monthly(),
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<MonthlyStatResponse[]>>('/statistics/monthly');
      return res.data.data;
    },
  });
}

export function useYearlyStat() {
  return useQuery({
    queryKey: statisticsKeys.yearly(),
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<YearlyStatResponse[]>>('/statistics/yearly');
      return res.data.data;
    },
  });
}

export function useUnitStat() {
  return useQuery({
    queryKey: statisticsKeys.unit(),
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<UnitStatResponse[]>>('/statistics/unit');
      return res.data.data;
    },
  });
}

export function useUnitRoster(unitId?: number) {
  return useQuery({
    queryKey: statisticsKeys.rosterUnit(unitId),
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<DeadRosterResponse[]>>(
        '/statistics/roster/unit',
        { params: { unitId } },
      );
      return res.data.data;
    },
    enabled: !!unitId,
  });
}

export function useAllRoster() {
  return useQuery({
    queryKey: statisticsKeys.rosterAll(),
    queryFn: async () => {
      const res = await apiClient.get<ApiResponse<DeadRosterResponse[]>>('/statistics/roster/all');
      return res.data.data;
    },
  });
}

// ─── Excel 다운로드 mutation 6종 ───

export function useExportBranchStatExcel() {
  return useMutation({
    mutationFn: async () => {
      const res = await apiClient.get('/statistics/branch/excel', { responseType: 'blob' });
      downloadBlob(res.data, `branch_stat_${new Date().toISOString().slice(0, 10)}.xlsx`);
    },
    onSuccess: () => message.success('Excel 다운로드가 완료되었습니다'),
    onError: () => message.error('Excel 다운로드에 실패했습니다'),
  });
}

export function useExportMonthlyStatExcel() {
  return useMutation({
    mutationFn: async () => {
      const res = await apiClient.get('/statistics/monthly/excel', { responseType: 'blob' });
      downloadBlob(res.data, `monthly_stat_${new Date().toISOString().slice(0, 10)}.xlsx`);
    },
    onSuccess: () => message.success('Excel 다운로드가 완료되었습니다'),
    onError: () => message.error('Excel 다운로드에 실패했습니다'),
  });
}

export function useExportYearlyStatExcel() {
  return useMutation({
    mutationFn: async () => {
      const res = await apiClient.get('/statistics/yearly/excel', { responseType: 'blob' });
      downloadBlob(res.data, `yearly_stat_${new Date().toISOString().slice(0, 10)}.xlsx`);
    },
    onSuccess: () => message.success('Excel 다운로드가 완료되었습니다'),
    onError: () => message.error('Excel 다운로드에 실패했습니다'),
  });
}

export function useExportUnitStatExcel() {
  return useMutation({
    mutationFn: async () => {
      const res = await apiClient.get('/statistics/unit/excel', { responseType: 'blob' });
      downloadBlob(res.data, `unit_stat_${new Date().toISOString().slice(0, 10)}.xlsx`);
    },
    onSuccess: () => message.success('Excel 다운로드가 완료되었습니다'),
    onError: () => message.error('Excel 다운로드에 실패했습니다'),
  });
}

export function useExportUnitRosterExcel() {
  return useMutation({
    mutationFn: async (unitId: number) => {
      const res = await apiClient.get('/statistics/roster/unit/excel', {
        params: { unitId },
        responseType: 'blob',
      });
      downloadBlob(res.data, `unit_roster_${new Date().toISOString().slice(0, 10)}.xlsx`);
    },
    onSuccess: () => message.success('Excel 다운로드가 완료되었습니다'),
    onError: () => message.error('Excel 다운로드에 실패했습니다'),
  });
}

export function useExportAllRosterExcel() {
  return useMutation({
    mutationFn: async () => {
      const res = await apiClient.get('/statistics/roster/all/excel', { responseType: 'blob' });
      downloadBlob(res.data, `all_roster_${new Date().toISOString().slice(0, 10)}.xlsx`);
    },
    onSuccess: () => message.success('Excel 다운로드가 완료되었습니다'),
    onError: () => message.error('Excel 다운로드에 실패했습니다'),
  });
}
