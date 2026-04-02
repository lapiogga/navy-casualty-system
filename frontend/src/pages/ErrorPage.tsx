import { Result, Button } from 'antd';
import { useNavigate } from 'react-router-dom';

export default function ErrorPage() {
  const navigate = useNavigate();

  return (
    <Result
      status="500"
      title="오류 발생"
      subTitle="페이지를 표시할 수 없습니다. 관리자에게 문의하세요."
      extra={
        <Button type="primary" onClick={() => navigate('/')}>
          홈으로 돌아가기
        </Button>
      }
    />
  );
}
