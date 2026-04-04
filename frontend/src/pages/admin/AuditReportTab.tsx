import { useState, useEffect } from 'react';
import { Button, DatePicker, Space, Typography, message } from 'antd';
import { FilePdfOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import type { Dayjs } from 'dayjs';
import { useAuditReport } from '../../hooks/useAdmin';

const { Title } = Typography;

/**
 * 감사 보고서 탭.
 * 연도/월을 선택하여 월별 감사 보고서를 PDF로 생성한다.
 */
export default function AuditReportTab() {
  const [selectedMonth, setSelectedMonth] = useState<Dayjs>(dayjs());
  const reportMutation = useAuditReport();
  const [blobUrl, setBlobUrl] = useState<string | null>(null);

  // Blob URL 메모리 누수 방지
  useEffect(() => {
    return () => {
      if (blobUrl) {
        URL.revokeObjectURL(blobUrl);
      }
    };
  }, [blobUrl]);

  const handleGenerate = () => {
    const year = selectedMonth.year();
    const month = selectedMonth.month() + 1;
    reportMutation.mutate(
      { year, month },
      {
        onSuccess: (blob: Blob) => {
          if (blobUrl) {
            URL.revokeObjectURL(blobUrl);
          }
          const url = URL.createObjectURL(blob);
          setBlobUrl(url);
          window.open(url, '_blank');
          message.success('보고서가 생성되었습니다');
        },
      },
    );
  };

  return (
    <div style={{ padding: 16 }}>
      <Title level={4}>월별 감사 보고서</Title>

      <Space direction="vertical" size="middle">
        <div>
          <Typography.Text strong>조회 기간: </Typography.Text>
          <DatePicker
            picker="month"
            value={selectedMonth}
            onChange={(value) => {
              if (value) setSelectedMonth(value);
            }}
            format="YYYY년 MM월"
            allowClear={false}
          />
        </div>

        <Button
          type="primary"
          icon={<FilePdfOutlined />}
          onClick={handleGenerate}
          loading={reportMutation.isPending}
        >
          PDF 생성
        </Button>
      </Space>
    </div>
  );
}
