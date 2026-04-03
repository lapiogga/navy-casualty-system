import { useMemo } from 'react';
import { Card, Table, Button, Spin } from 'antd';
import { DownloadOutlined } from '@ant-design/icons';
import { Column } from '@ant-design/charts';
import { useYearlyStat, useExportYearlyStatExcel } from '../../api/statistics';

export default function YearlyStatPage() {
  const { data, isLoading } = useYearlyStat();
  const exportExcel = useExportYearlyStatExcel();

  // 차트용: year를 문자열로 변환
  const chartData = useMemo(
    () => (data || []).map((d) => ({ ...d, yearLabel: String(d.year) })),
    [data],
  );

  const chartConfig = {
    data: chartData,
    xField: 'yearLabel' as const,
    yField: 'count' as const,
    label: {
      text: (d: { count: number }) => `${d.count}`,
      textBaseline: 'bottom' as const,
    },
    style: { radiusEndTop: 2, radiusEndBottom: 2 },
  };

  const columns = [
    { title: '연도', dataIndex: 'year', key: 'year' },
    { title: '인원수', dataIndex: 'count', key: 'count', align: 'right' as const },
  ];

  const total = data?.reduce((sum, d) => sum + d.count, 0) || 0;

  return (
    <Spin spinning={isLoading}>
      <Card
        title="연도별 사망자 현황"
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
        rowKey="year"
        pagination={false}
        style={{ marginTop: 16 }}
        summary={() => (
          <Table.Summary.Row>
            <Table.Summary.Cell index={0}>합계</Table.Summary.Cell>
            <Table.Summary.Cell index={1} align="right">
              {total}
            </Table.Summary.Cell>
          </Table.Summary.Row>
        )}
      />
    </Spin>
  );
}
