import { Drawer, Timeline, Descriptions, Tag, Spin, Empty } from 'antd';
import { useReviewHistories } from '../../api/review';
import { CLASSIFICATION_LABELS } from '../../types/review';
import type { ReviewClassification } from '../../types/review';

interface Props {
  reviewId: number | null;
  open: boolean;
  onClose: () => void;
}

export default function ReviewHistoryDrawer({ reviewId, open, onClose }: Props) {
  const { data: histories, isLoading } = useReviewHistories(reviewId);

  const getColor = (classification: string | null) => {
    if (classification === 'REJECTED') return 'red';
    if (classification === 'DEFERRED') return 'gray';
    return 'blue';
  };

  return (
    <Drawer title="심사 이력" open={open} onClose={onClose} width={600}>
      {isLoading && <Spin />}
      {!isLoading && (!histories || histories.length === 0) && (
        <Empty description="변경 이력이 없습니다" />
      )}
      {!isLoading && histories && histories.length > 0 && (
        <Timeline
          items={histories.map((h) => ({
            color: getColor(h.snapshot?.classification),
            children: (
              <div key={h.id}>
                <p>
                  <strong>{h.reviewRound}차 심사</strong> —{' '}
                  {new Date(h.changedAt).toLocaleString('ko-KR')}
                </p>
                <Descriptions size="small" column={1} bordered>
                  <Descriptions.Item label="분류">
                    {h.snapshot?.classification ? (
                      <Tag>
                        {CLASSIFICATION_LABELS[h.snapshot.classification as ReviewClassification] ||
                          h.snapshot.classification}
                      </Tag>
                    ) : (
                      '-'
                    )}
                  </Descriptions.Item>
                  <Descriptions.Item label="소속부대 심사결과">
                    {h.snapshot?.unitReviewResult || '-'}
                  </Descriptions.Item>
                  <Descriptions.Item label="병명">
                    {h.snapshot?.diseaseName || '-'}
                  </Descriptions.Item>
                  <Descriptions.Item label="상태">
                    {h.snapshot?.status || '-'}
                  </Descriptions.Item>
                  <Descriptions.Item label="변경자">
                    {h.changedBy || '-'}
                  </Descriptions.Item>
                </Descriptions>
              </div>
            ),
          }))}
        />
      )}
    </Drawer>
  );
}
