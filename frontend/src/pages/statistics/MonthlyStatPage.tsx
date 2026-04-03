import { useMemo } from 'react';
import { Card, Table, Button, Spin } from 'antd';
import { DownloadOutlined } from '@ant-design/icons';
import { Column } from '@ant-design/charts';
import { useMonthlyStat, useExportMonthlyStatExcel } from '../../api/statistics';

export default function MonthlyStatPage() {
  const { data, isLoading } = useMonthlyStat();
  const exportExcel = useExportMonthlyStatExcel();

  // 차트용 라벨 가공
  const chartData = useMemo(
    () =>
      (data || []).map((d) => ({
        ...d,
        label: `${d.year}년 ${d.month}월`,
      })),
    [data],
  );

  const chartConfig = {
    data: chartData,
    xField: 'label' as const,
    yField: 'count' as const,
    label: {
      text: (d: { count: number }) => `${d.count}`,
      textBaseline: 'bottom' as const,
    },
    style: { radiusEndTop: 2, radiusEndBottom: 2 },
  };

  const columns = [
    { title: '연도', dataIndex: 'year', key: 'year' },
    { title: '월', dataIndex: 'month', key: 'month' },
    { title: '인원수', dataIndex: 'count', key: 'count', align: 'right' as const },
  ];

  const total = data?.reduce((sum, d) => sum + d.count, 0) || 0;

  return (
    <Spin spinning={isLoading}>
      <Card
        title="월별 사망자 현황"
        extra={
          <Button
            icon={<DownloadOutlined />}
            onClick={() => exportExcel.mutate()}
            loading={exportExcel.isPending}
          >
            Excel 다운로드
          </Button>
        }
      >
        <Column {...chartConfig} height={300} />
      </Card>
      <Table
        columns={columns}
        dataSource={data}
        rowKey={(r) => `${r.year}-${r.month}`}
        pagination={false}
        style={{ marginTop: 16 }}
        summary={() => (
          <Table.Summary.Row>
            <Table.Summary.Cell index={0} colSpan={2}>
              합계
            </Table.Summary.Cell>
            <Table.Summary.Cell index={2} align="right">
              {total}
            </Table.Summary.Cell>
          </Table.Summary.Row>
        )}
      />
    </Spin>
  );
}
