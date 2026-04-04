import { useState } from 'react';
import { Form, Input, Button, Alert, Card } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import apiClient from '../api/client';

interface ChangePasswordFormValues {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export default function ChangePasswordPage() {
  const { user, refreshUser } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const isFirstLogin = user && !user.passwordChanged;

  const onFinish = async (values: ChangePasswordFormValues) => {
    setError(null);
    setLoading(true);
    try {
      await apiClient.put('/auth/change-password', {
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      });
      await refreshUser();
      navigate('/dead', { replace: true });
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data
          ?.message ?? '비밀번호 변경에 실패했습니다';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100vh',
        background: '#f0f2f5',
      }}
    >
      <Card style={{ width: 420 }}>
        <h2 style={{ textAlign: 'center', marginBottom: 8 }}>비밀번호 변경</h2>

        {isFirstLogin && (
          <Alert
            type="info"
            message="초기 비밀번호를 변경해주세요"
            description="보안을 위해 첫 로그인 시 비밀번호를 변경해야 합니다."
            showIcon
            style={{ marginBottom: 16 }}
          />
        )}

        {error && (
          <Alert
            type="error"
            message={error}
            showIcon
            style={{ marginBottom: 16 }}
          />
        )}

        <Form<ChangePasswordFormValues> onFinish={onFinish} layout="vertical">
          <Form.Item
            name="currentPassword"
            label="현재 비밀번호"
            rules={[{ required: true, message: '현재 비밀번호를 입력하세요' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="현재 비밀번호"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="newPassword"
            label="새 비밀번호"
            rules={[
              { required: true, message: '새 비밀번호를 입력하세요' },
              { min: 8, message: '비밀번호는 8자 이상이어야 합니다' },
              {
                pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/,
                message: '영문과 숫자를 포함해야 합니다',
              },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="새 비밀번호 (8자 이상, 영문+숫자)"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="새 비밀번호 확인"
            dependencies={['newPassword']}
            rules={[
              { required: true, message: '새 비밀번호를 다시 입력하세요' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(
                    new Error('새 비밀번호가 일치하지 않습니다'),
                  );
                },
              }),
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="새 비밀번호 확인"
              size="large"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              size="large"
            >
              비밀번호 변경
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
