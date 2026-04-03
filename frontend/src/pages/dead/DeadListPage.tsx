import { useState } from 'react';
import { Button, Card, Space } from 'antd';
import { DownloadOutlined } from '@ant-design/icons';
import { useExportDeadExcel } from '../../api/dead';
import type { DeadSearchParams } from '../../types/dead';

/**
 * 사망자 목록 페이지.
 * 검색 폼 + 테이블 + Excel 다운로드 버튼.
 */
export default function DeadListPage() {
  const [searchParams] = useState<DeadSearchParams>({});
  const exportExcel = useExportDeadExcel();

  return (
    <Card title="사망자 현황">
      <Space style={{ marginBottom: 16 }}>
        <Button
          icon={<DownloadOutlined />}
          onClick={() => exportExcel.mutate(searchParams)}
          loading={exportExcel.isPending}
        >
          Excel 다운로드
        </Button>
      </Space>
    </Card>
  );
}
