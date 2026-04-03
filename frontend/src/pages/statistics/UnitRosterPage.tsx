import { useState } from 'react';
import { Card, Table, Select, Button, Tag, Space, Spin } from 'antd';
import { DownloadOutlined } from '@ant-design/icons';
import { useUnits } from '../../api/dead';
import { useUnitRoster, useExportUnitRosterExcel } from '../../api/statistics';
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

export default function UnitRosterPage() {
  const [unitId, setUnitId] = useState<number | undefined>();
  const { data: units } = useUnits();
  const { data, isLoading } = useUnitRoster(unitId);
  const exportExcel = useExportUnitRosterExcel();

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
        title="부대별 사망자 명부"
        extra={
          <Space>
            <Select
              placeholder="부대 선택"
              allowClear
              style={{ width: 200 }}
              value={unitId}
              onChange={(v) => setUnitId(v)}
              options={units?.map((u) => ({ value: u.id, label: u.unitName }))}
            />
            <Button
              icon={<DownloadOutlined />}
              disabled={!unitId}
              onClick={() => unitId && exportExcel.mutate(unitId)}
              loading={exportExcel.isPending}
            >
              Excel 다운로드
            </Button>
          </Space>
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
