import { Modal, Form, Input } from 'antd';
import { useDeleteReview } from '../../api/review';

interface ReviewDeleteModalProps {
  open: boolean;
  onClose: () => void;
  recordId: number | null;
}

export default function ReviewDeleteModal({ open, onClose, recordId }: ReviewDeleteModalProps) {
  const [form] = Form.useForm();
  const deleteReview = useDeleteReview();

  const handleOk = async () => {
    const values = await form.validateFields();
    if (recordId === null) return;

    deleteReview.mutate(
      { id: recordId, reason: values.reason },
      {
        onSuccess: () => {
          form.resetFields();
          onClose();
        },
      },
    );
  };

  return (
    <Modal
      title="전공사상심사 삭제"
      open={open}
      onCancel={onClose}
      onOk={handleOk}
      okText="삭제"
      cancelText="취소"
      okButtonProps={{ danger: true }}
      confirmLoading={deleteReview.isPending}
      destroyOnClose
    >
      <p>삭제 시 복구가 불가합니다. 삭제 사유를 입력하세요.</p>
      <Form form={form} layout="vertical">
        <Form.Item
          name="reason"
          label="삭제 사유"
          rules={[{ required: true, message: '삭제 사유를 입력하세요' }]}
        >
          <Input.TextArea rows={3} placeholder="삭제 사유를 입력하세요" />
        </Form.Item>
      </Form>
    </Modal>
  );
}
