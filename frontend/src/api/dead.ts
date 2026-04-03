import { useMutation } from '@tanstack/react-query';
import { message } from 'antd';
import apiClient from './client';
import type { DeadSearchParams } from '../types/dead';

/**
 * 사망자 react-query 키.
 */
export const deadKeys = {
  all: ['dead'] as const,
  lists: () => [...deadKeys.all, 'list'] as const,
  list: (params: DeadSearchParams) => [...deadKeys.lists(), params] as const,
};

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
