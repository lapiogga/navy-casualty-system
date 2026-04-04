import { useEffect } from 'react';
import { Modal, Form, Input, InputNumber, Select, DatePicker } from 'antd';
import dayjs from 'dayjs';
import { validateRrn } from '../../utils/rrnValidation';
import {
  useCreateReview,
  useUpdateReview,
  useRanks,
  useBranches,
  useUnits,
} from '../../api/review';
import type { ReviewRecord } from '../../types/review';

const classificationOptions = [
  { value: 'COMBAT_WOUND', label: '전공상' },
  { value: 'DUTY_WOUND', label: '공상' },
  { value: 'REJECTED', label: '기각' },
  { value: 'DEFERRED', label: '보류' },
];

interface ReviewFormModalProps {
  open: boolean;
  onClose: () => void;
  editRecord?: ReviewRecord | null;
}

export default function ReviewFormModal({ open, onClose, editRecord }: ReviewFormModalProps) {
  const [form] = Form.useForm();
  const createReview = useCreateReview();
  const updateReview = useUpdateReview();
  const { data: ranks } = useRanks();
  const { data: branches } = useBranches();
  const { data: units } = useUnits();

  const isEdit = !!editRecord;

  useEffect(() => {
    if (open && editRecord) {
      form.setFieldsValue({
        reviewRound: editRecord.reviewRound,
        reviewDate: editRecord.reviewDate ? dayjs(editRecord.reviewDate) : null,
        serviceNumber: editRecord.serviceNumber,
        name: editRecord.name,
        birthDate: editRecord.birthDate ? dayjs(editRecord.birthDate) : null,
        enlistmentDate: editRecord.enlistmentDate ? dayjs(editRecord.enlistmentDate) : null,
        diseaseName: editRecord.diseaseName,
        unitReviewResult: editRecord.unitReviewResult,
      });
    }
    if (open && !editRecord) {
      form.resetFields();
    }
  }, [open, editRecord, form]);

  const handleOk = async () => {
    const values = await form.validateFields();
    const payload = {
      ...values,
      reviewDate: values.reviewDate?.format('YYYY-MM-DD') || undefined,
      birthDate: values.birthDate?.format('YYYY-MM-DD') || undefined,
      enlistmentDate: values.enlistmentDate?.format('YYYY-MM-DD') || undefined,
    };

    if (isEdit && editRecord) {
      const { serviceNumber: _sn, ...updateData } = payload;
      updateReview.mutate(
        { id: editRecord.id, data: updateData },
        { onSuccess: () => { form.resetFields(); onClose(); } },
      );
    } else {
      createReview.mutate(payload, {
        onSuccess: () => { form.resetFields(); onClose(); },
      });
    }
  };

  const handleSsnChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const ssn = e.target.value;
    // 주민번호에서 생년월일 자동 추출 (YYMMDD-N...)
    const digits = ssn.replace('-', '');
    if (digits.length >= 7) {
      const yearPrefix = Number(digits[6]) <= 2 ? '19' : '20';
      const birthStr = `${yearPrefix}${digits.substring(0, 2)}-${digits.substring(2, 4)}-${digits.substring(4, 6)}`;
      const parsed = dayjs(birthStr, 'YYYY-MM-DD');
      if (parsed.isValid()) {
        form.setFieldValue('birthDate', parsed);
      }
    }
  };

  return (
    <Modal
      title={isEdit ? '전공사상심사 수정' : '전공사상심사 등록'}
      open={open}
      onCancel={onClose}
      onOk={handleOk}
      okText={isEdit ? '수정' : '등록'}
      cancelText="취소"
      confirmLoading={createReview.isPending || updateReview.isPending}
      width={640}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="reviewRound"
          label="심사차수"
          rules={[{ required: true, message: '심사차수를 입력하세요' }]}
        >
          <InputNumber min={1} placeholder="심사차수" style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item name="reviewDate" label="심사일자">
          <DatePicker style={{ width: '100%' }} placeholder="심사일자 선택" />
        </Form.Item>

        <Form.Item
          name="serviceNumber"
          label="군번"
          rules={[{ required: true, message: '군번을 입력하세요' }]}
        >
          <Input disabled={isEdit} placeholder="군번 입력" />
        </Form.Item>

        <Form.Item
          name="name"
          label="성명"
          rules={[{ required: true, message: '성명을 입력하세요' }]}
        >
          <Input placeholder="성명 입력" />
        </Form.Item>

        <Form.Item
          name="ssn"
          label="주민등록번호"
          rules={[
            {
              validator: (_, value: string) => {
                if (!value) return Promise.resolve();
                if (!validateRrn(value)) {
                  return Promise.reject(new Error('유효하지 않은 주민등록번호입니다'));
                }
                return Promise.resolve();
              },
            },
          ]}
        >
          <Input placeholder="YYMMDD-NNNNNNN" onChange={handleSsnChange} />
        </Form.Item>

        <Form.Item name="birthDate" label="생년월일">
          <DatePicker style={{ width: '100%' }} placeholder="생년월일 선택" />
        </Form.Item>

        <Form.Item name="rankId" label="계급">
          <Select
            allowClear
            placeholder="계급 선택"
            options={ranks?.map((r) => ({ value: r.id, label: r.rankName }))}
          />
        </Form.Item>

        <Form.Item name="branchId" label="군구분">
          <Select
            allowClear
            placeholder="군구분 선택"
            options={branches?.map((b) => ({ value: b.id, label: b.branchName }))}
          />
        </Form.Item>

        <Form.Item name="unitId" label="소속">
          <Select
            allowClear
            placeholder="소속 선택"
            options={units?.map((u) => ({ value: u.id, label: u.unitName }))}
          />
        </Form.Item>

        <Form.Item name="enlistmentDate" label="입대일자">
          <DatePicker style={{ width: '100%' }} placeholder="입대일자 선택" />
        </Form.Item>

        <Form.Item name="diseaseName" label="병명">
          <Input placeholder="병명 입력" maxLength={200} />
        </Form.Item>

        <Form.Item name="unitReviewResult" label="소속부대 심사결과">
          <Input placeholder="소속부대 심사결과 입력" />
        </Form.Item>

        <Form.Item name="classification" label="전공상 분류">
          <Select allowClear placeholder="분류 선택" options={classificationOptions} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
