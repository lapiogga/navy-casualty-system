import { useState } from 'react';
import { Button, Radio, Upload, Table, Alert, Space, Typography, message } from 'antd';
import { UploadOutlined } from '@ant-design/icons';
import type { UploadFile } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useImportExcel } from '../../hooks/useAdmin';

const { Title } = Typography;

interface ImportError {
  rowNumber: number;
  column: string;
  reason: string;
}

const errorColumns: ColumnsType<ImportError> = [
  { title: '행번호', dataIndex: 'rowNumber', key: 'rowNumber', width: 80 },
  { title: '컬럼', dataIndex: 'column', key: 'column', width: 120 },
  { title: '사유', dataIndex: 'reason', key: 'reason' },
];

/**
 * 데이터 임포트 탭.
 * Excel 파일을 업로드하여 사망자/상이자/심사 데이터를 일괄 등록한다.
 */
export default function DataImportTab() {
  const [importType, setImportType] = useState<string>('dead');
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const importMutation = useImportExcel();

  const handleImport = () => {
    if (fileList.length === 0) {
      message.warning('파일을 선택해주세요');
      return;
    }
    const file = fileList[0].originFileObj;
    if (!file) {
      message.warning('유효한 파일이 아닙니다');
      return;
    }
    importMutation.mutate({ type: importType, file });
  };

  const result = importMutation.data;

  return (
    <div style={{ padding: 16 }}>
      <Title level={4}>Excel 데이터 임포트</Title>

      <Space direction="vertical" size="middle" style={{ width: '100%' }}>
        <div>
          <Typography.Text strong>임포트 타입: </Typography.Text>
          <Radio.Group value={importType} onChange={(e) => setImportType(e.target.value)}>
            <Radio.Button value="dead">사망자</Radio.Button>
            <Radio.Button value="wounded">상이자</Radio.Button>
            <Radio.Button value="review">전공사상심사</Radio.Button>
          </Radio.Group>
        </div>

        <Upload
          accept=".xlsx,.xls"
          fileList={fileList}
          beforeUpload={() => false}
          onChange={({ fileList: newFileList }) => setFileList(newFileList.slice(-1))}
          maxCount={1}
        >
          <Button icon={<UploadOutlined />}>Excel 파일 선택</Button>
        </Upload>

        <Button
          type="primary"
          onClick={handleImport}
          loading={importMutation.isPending}
          disabled={fileList.length === 0}
        >
          임포트 실행
        </Button>

        {result && (
          <>
            <Alert
              type={result.errorRows > 0 ? 'warning' : 'success'}
              message={`전체 ${result.totalRows}건 중 성공 ${result.successRows}건, 실패 ${result.errorRows}건`}
              showIcon
            />
            {result.errors.length > 0 && (
              <Table<ImportError>
                columns={errorColumns}
                dataSource={result.errors}
                rowKey={(r) => `${r.rowNumber}-${r.column}`}
                size="small"
                pagination={{ pageSize: 20 }}
                scroll={{ y: 400 }}
              />
            )}
          </>
        )}
      </Space>
    </div>
  );
}
