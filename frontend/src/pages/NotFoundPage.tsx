import { Result, Button } from 'antd';
import { useNavigate } from 'react-router-dom';

export default function NotFoundPage() {
  const navigate = useNavigate();

  return (
    <Result
      status="404"
      title="요청한 페이지를 찾을 수 없습니다"
      subTitle="URL을 확인하거나 메뉴에서 다시 선택하세요."
      extra={
        <Button type="primary" onClick={() => navigate('/')}>
          홈으로 돌아가기
        </Button>
      }
    />
  );
}
