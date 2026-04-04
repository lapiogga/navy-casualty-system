import { useEffect } from 'react';
import { Modal, Form, Input, Select, DatePicker } from 'antd';
import dayjs from 'dayjs';
import { validateRrn } from '../../utils/rrnValidation';
import { useCreateDead, useUpdateDead, useRanks, useBranches, useUnits, useDeathTypes, useDeathCodes } from '../../api/dead';
import type { DeadRecord } from '../../types/dead';

interface DeadFormModalProps {
  open: boolean;
  onClose: () => void;
  editRecord?: DeadRecord | null;
}

export default function DeadFormModal({ open, onClose, editRecord }: DeadFormModalProps) {
  const [form] = Form.useForm();
  const createDead = useCreateDead();
  const updateDead = useUpdateDead();
  const { data: ranks } = useRanks();
  const { data: branches } = useBranches();
  const { data: units } = useUnits();
  const { data: deathTypes } = useDeathTypes();
  const { data: deathCodes } = useDeathCodes();

  const isEdit = !!editRecord;

  useEffect(() => {
    if (open && editRecord) {
      form.setFieldsValue({
        serviceNumber: editRecord.serviceNumber,
        name: editRecord.name,
        birthDate: editRecord.birthDate ? dayjs(editRecord.birthDate) : null,
        enlistmentDate: editRecord.enlistmentDate ? dayjs(editRecord.enlistmentDate) : null,
        deathDate: editRecord.deathDate ? dayjs(editRecord.deathDate) : null,
        phone: editRecord.phone,
        address: editRecord.address,
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
      deathDate: values.deathDate?.format('YYYY-MM-DD'),
    };

    if (isEdit && editRecord) {
      const { serviceNumber: _sn, ...updateData } = payload;
      updateDead.mutate(
        { id: editRecord.id, data: updateData },
        { onSuccess: () => { form.resetFields(); onClose(); } },
      );
    } else {
      createDead.mutate(payload, {
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
      title={isEdit ? '사망자 수정' : '사망자 등록'}
      open={open}
      onCancel={onClose}
      onOk={handleOk}
      okText={isEdit ? '수정' : '등록'}
      cancelText="취소"
      confirmLoading={createDead.isPending || updateDead.isPending}
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

        <Form.Item name="deathTypeId" label="사망구분">
          <Select
            allowClear
            placeholder="사망구분 선택"
            options={deathTypes?.map((t) => ({ value: t.id, label: t.typeName }))}
          />
        </Form.Item>

        <Form.Item name="deathCodeId" label="사망코드">
          <Select
            allowClear
            placeholder="사망코드 선택"
            options={deathCodes?.map((c) => ({ value: c.id, label: `${c.codeSymbol} - ${c.codeName}` }))}
          />
        </Form.Item>

        <Form.Item name="address" label="주소">
          <Input placeholder="주소 입력" />
        </Form.Item>

        <Form.Item
          name="deathDate"
          label="사망일자"
          rules={[{ required: true, message: '사망일자를 선택하세요' }]}
        >
          <DatePicker style={{ width: '100%' }} placeholder="사망일자 선택" />
        </Form.Item>
      </Form>
    </Modal>
  );
}
