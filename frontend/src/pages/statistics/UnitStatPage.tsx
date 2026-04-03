import { Card, Table, Button, Spin } from 'antd';
import { DownloadOutlined } from '@ant-design/icons';
import { Column } from '@ant-design/charts';
import { useUnitStat, useExportUnitStatExcel } from '../../api/statistics';
import type { UnitStatResponse } from '../../types/statistics';

export default function UnitStatPage() {
  const { data, isLoading } = useUnitStat();
  const exportExcel = useExportUnitStatExcel();

  const chartConfig = {
    data: data || [],
    xField: 'unitName' as const,
    yField: 'count' as const,
    label: {
      text: (d: UnitStatResponse) => `${d.count}`,
      textBaseline: 'bottom' as const,
    },
    style: { radiusEndTop: 2, radiusEndBottom: 2 },
  };

  const columns = [
    { title: '부대', dataIndex: 'unitName', key: 'unitName' },
    { title: '인원수', dataIndex: 'count', key: 'count', align: 'right' as const },
  ];

  const total = data?.reduce((sum, d) => sum + d.count, 0) || 0;

  return (
    <Spin spinning={isLoading}>
      <Card
        title="부대별 사망자 현황"
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
        rowKey="unitName"
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
