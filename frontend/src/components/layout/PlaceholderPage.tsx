import { Empty } from 'antd';

interface PlaceholderPageProps {
  title: string;
}

export default function PlaceholderPage({ title }: PlaceholderPageProps) {
  return (
    <div style={{ textAlign: 'center', padding: '80px 0' }}>
      <h2>{title}</h2>
      <Empty description="이 기능은 아직 개발 중입니다." />
    </div>
  );
}
