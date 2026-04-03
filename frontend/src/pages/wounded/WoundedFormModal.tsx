import { useEffect } from 'react';
import { Modal, Form, Input, Select, DatePicker } from 'antd';
import dayjs from 'dayjs';
import { validateRrn } from '../../utils/rrnValidation';
import {
  useCreateWounded,
  useUpdateWounded,
  useRanks,
  useBranches,
  useUnits,
  useVeteransOffices,
} from '../../api/wounded';
import type { WoundedRecord } from '../../types/wounded';

const woundTypeOptions = [
  { value: 'COMBAT_WOUND', label: '전공상' },
  { value: 'DUTY_WOUND', label: '공상' },
  { value: 'GENERAL_WOUND', label: '일반상이' },
];

interface WoundedFormModalProps {
  open: boolean;
  onClose: () => void;
  editRecord?: WoundedRecord | null;
}

export default function WoundedFormModal({ open, onClose, editRecord }: WoundedFormModalProps) {
  const [form] = Form.useForm();
  const createWounded = useCreateWounded();
  const updateWounded = useUpdateWounded();
  const { data: ranks } = useRanks();
  const { data: branches } = useBranches();
  const { data: units } = useUnits();
  const { data: veteransOffices } = useVeteransOffices();

  const isEdit = !!editRecord;

  useEffect(() => {
    if (open && editRecord) {
      form.setFieldsValue({
        serviceNumber: editRecord.serviceNumber,
        name: editRecord.name,
        birthDate: editRecord.birthDate ? dayjs(editRecord.birthDate) : null,
        enlistmentDate: editRecord.enlistmentDate ? dayjs(editRecord.enlistmentDate) : null,
        phone: editRecord.phone,
        address: editRecord.address,
        diseaseName: editRecord.diseaseName,
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
      birthDate: values.birthDate?.format('YYYY-MM-DD'),
      enlistmentDate: values.enlistmentDate?.format('YYYY-MM-DD') || undefined,
    };

    if (isEdit && editRecord) {
      const { serviceNumber: _, ...updateData } = payload;
      updateWounded.mutate(
        { id: editRecord.id, data: updateData },
        { onSuccess: () => { form.resetFields(); onClose(); } },
      );
    } else {
      createWounded.mutate(payload, {
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
      title={isEdit ? '상이자 수정' : '상이자 등록'}
      open={open}
      onCancel={onClose}
      onOk={handleOk}
      okText={isEdit ? '수정' : '등록'}
      cancelText="취소"
      confirmLoading={createWounded.isPending || updateWounded.isPending}
      width={640}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
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
            { required: true, message: '주민등록번호를 입력하세요' },
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

        <Form.Item
          name="birthDate"
          label="생년월일"
          rules={[{ required: true, message: '생년월일을 선택하세요' }]}
        >
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

        <Form.Item name="phone" label="전화번호">
          <Input placeholder="전화번호 입력" />
        </Form.Item>

        <Form.Item name="address" label="주소">
          <Input placeholder="주소 입력" />
        </Form.Item>

        <Form.Item name="veteransOfficeId" label="보훈청명">
          <Select
            allowClear
            showSearch
            optionFilterProp="children"
            placeholder="보훈청 선택"
            options={veteransOffices?.map((v) => ({ value: v.id, label: v.officeName }))}
          />
        </Form.Item>

        <Form.Item name="diseaseName" label="병명">
          <Input placeholder="병명 입력" maxLength={200} />
        </Form.Item>

        <Form.Item
          name="woundType"
          label="상이구분"
          rules={[{ required: true, message: '상이구분을 선택하세요' }]}
        >
          <Select placeholder="상이구분 선택" options={woundTypeOptions} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
