import { Card, Table, Button, Tag, Spin } from 'antd';
import { DownloadOutlined } from '@ant-design/icons';
import { useAllRoster, useExportAllRosterExcel } from '../../api/statistics';
import type { DeadRosterResponse } from '../../types/statistics';

const statusColorMap: Record<string, string> = {
  REGISTERED: 'blue',
  CONFIRMED: 'green',
  NOTIFIED: 'gold',
};

const statusLabelMap: Record<string, string> = {
  REGISTERED: '등록',
  CONFIRMED: '확인',
  NOTIFIED: '통보',
};

export default function AllRosterPage() {
  const { data, isLoading } = useAllRoster();
  const exportExcel = useExportAllRosterExcel();

  const columns = [
    {
      title: '번호',
      key: 'index',
      width: 60,
      render: (_: unknown, __: DeadRosterResponse, index: number) => index + 1,
    },
    { title: '군구분', dataIndex: 'branchName', key: 'branchName', width: 80 },
    { title: '군번', dataIndex: 'serviceNumber', key: 'serviceNumber', width: 120 },
    { title: '성명', dataIndex: 'name', key: 'name', width: 80 },
    { title: '주민번호', dataIndex: 'ssnMasked', key: 'ssnMasked', width: 140 },
    { title: '계급', dataIndex: 'rankName', key: 'rankName', width: 80 },
    { title: '소속', dataIndex: 'unitName', key: 'unitName', width: 120 },
    { title: '사망일자', dataIndex: 'deathDate', key: 'deathDate', width: 110 },
    { title: '사망구분', dataIndex: 'deathTypeName', key: 'deathTypeName', width: 100 },
    {
      title: '상태',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: string) => (
        <Tag color={statusColorMap[status] || 'default'}>
          {statusLabelMap[status] || status}
        </Tag>
      ),
    },
  ];

  return (
    <Spin spinning={isLoading}>
      <Card
        title="전사망자 명부"
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
        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          pagination={{ showSizeChanger: true, showTotal: (total) => `총 ${total}건` }}
          scroll={{ x: 1000 }}
          size="small"
        />
      </Card>
    </Spin>
  );
}
