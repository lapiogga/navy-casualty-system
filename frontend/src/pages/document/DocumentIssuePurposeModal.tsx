import { Modal, Form, Input } from 'antd';
import { useGenerateDocument } from '../../api/document';
import type { DocumentType } from '../../types/document';

interface Props {
  open: boolean;
  documentType: DocumentType;
  targetId: number;
  onSuccess: (blob: Blob) => void;
  onCancel: () => void;
}

/**
 * 발급 목적 입력 Modal (DOCU-08).
 * 목적 입력 후 문서 생성 API를 호출하여 PDF blob을 onSuccess로 전달한다.
 */
export default function DocumentIssuePurposeModal({
  open,
  documentType,
  targetId,
  onSuccess,
  onCancel,
}: Props) {
  const [form] = Form.useForm<{ purpose: string }>();
  const generateDocument = useGenerateDocument();

  const handleOk = async () => {
    const values = await form.validateFields();
    const blob = await generateDocument.mutateAsync({
      documentType,
      targetId,
      purpose: values.purpose,
    });
    form.resetFields();
    onSuccess(blob);
  };

  const handleCancel = () => {
    form.resetFields();
    onCancel();
  };

  return (
    <Modal
      title="발급 목적 입력"
      open={open}
      onOk={handleOk}
      onCancel={handleCancel}
      okText="확인"
      cancelText="취소"
      okButtonProps={{ loading: generateDocument.isPending }}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="purpose"
          label="발급 목적"
          rules={[
            { required: true, message: '발급 목적을 입력하세요' },
            { max: 500, message: '500자 이내로 입력하세요' },
          ]}
        >
          <Input.TextArea rows={3} placeholder="발급 목적을 입력하세요" maxLength={500} showCount />
        </Form.Item>
      </Form>
    </Modal>
  );
}
