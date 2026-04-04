#!/bin/bash
# 5년 초과 감사 로그 파티션 아카이브 스크립트.
# 매년 1월에 cron 등으로 실행하여 오래된 파티션을 백업한다.

set -euo pipefail

YEAR_TO_ARCHIVE=$(($(date +%Y) - 5))
PARTITION="tb_audit_log_${YEAR_TO_ARCHIVE}"
BACKUP_DIR="/backup/audit-archive"

mkdir -p "$BACKUP_DIR"

echo "=== 감사 로그 아카이브 시작 ==="
echo "대상 파티션: $PARTITION"
echo "백업 경로: $BACKUP_DIR"

# 파티션 존재 여부 확인
PARTITION_EXISTS=$(psql -U "${DB_USER:-casualty}" -d "${DB_NAME:-casualty}" -tAc \
    "SELECT 1 FROM pg_tables WHERE tablename = '$PARTITION'")

if [ "$PARTITION_EXISTS" != "1" ]; then
    echo "파티션 '$PARTITION'이 존재하지 않습니다. 종료합니다."
    exit 0
fi

# pg_dump로 백업
BACKUP_FILE="$BACKUP_DIR/${PARTITION}_$(date +%Y%m%d).sql"
pg_dump -U "${DB_USER:-casualty}" -d "${DB_NAME:-casualty}" \
    -t "$PARTITION" -f "$BACKUP_FILE"

echo "아카이브 완료: $BACKUP_FILE"
echo ""
echo "파티션 삭제는 수동으로 실행하세요:"
echo "  DROP TABLE $PARTITION;"
