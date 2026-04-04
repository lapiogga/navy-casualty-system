import { useState, useEffect } from 'react';
import { Form, Input, Button, Alert } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

interface LoginFormValues {
  username: string;
  password: string;
}

export default function LoginPage() {
  const { login, user } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // 이미 로그인된 상태이면 passwordChanged 여부에 따라 리다이렉트
  useEffect(() => {
    if (user) {
      if (!user.passwordChanged) {
        navigate('/change-password', { replace: true });
      } else {
        navigate('/dead', { replace: true });
      }
    }
  }, [user, navigate]);

  const onFinish = async (values: LoginFormValues) => {
    setError(null);
    setLoading(true);
    try {
      await login(values.username, values.password);
      // login이 성공하면 user 상태가 업데이트되고 useEffect에서 리다이렉트
    } catch {
      setError('사용자 ID 또는 비밀번호가 올바르지 않습니다');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', height: '100vh' }}>
      {/* 좌측: 시스템 소개 */}
      <div
        style={{
          flex: 1,
          background: '#f5f5f5',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <h1 style={{ fontSize: 28, fontWeight: 700, marginBottom: 8 }}>
          해군 사상자 관리 시스템
        </h1>
        <p style={{ fontSize: 16, color: '#666' }}>해군본부 인사참모부</p>
      </div>

      {/* 우측: 로그인 폼 */}
      <div
        style={{
          flex: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <div style={{ width: 360 }}>
          <h2 style={{ textAlign: 'center', marginBottom: 32 }}>로그인</h2>

          {error && (
            <Alert
              type="error"
              message={error}
              showIcon
              style={{ marginBottom: 16 }}
            />
          )}

          <Form<LoginFormValues> onFinish={onFinish} layout="vertical">
            <Form.Item
              name="username"
              rules={[{ required: true, message: '사용자 ID를 입력하세요' }]}
            >
              <Input
                prefix={<UserOutlined />}
                placeholder="사용자 ID"
                size="large"
              />
            </Form.Item>

            <Form.Item
              name="password"
              rules={[{ required: true, message: '비밀번호를 입력하세요' }]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="비밀번호"
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
                로그인
              </Button>
            </Form.Item>
          </Form>
        </div>
      </div>
    </div>
  );
}
