#!/bin/bash
set -euo pipefail
BACKUP_DIR="${BACKUP_DIR:-./backups}"
DB_USER="${DB_USER:-casualty}"
DB_NAME="${DB_NAME:-casualty}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
RETENTION_DAYS=7

mkdir -p "$BACKUP_DIR"
FILENAME="$BACKUP_DIR/${DB_NAME}_$(date +%Y%m%d_%H%M%S).sql.gz"

echo "백업 시작: $DB_NAME -> $FILENAME"
PGPASSWORD="${DB_PASSWORD}" pg_dump \
  -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME" \
  | gzip > "$FILENAME"

echo "백업 완료: $(ls -lh "$FILENAME" | awk '{print $5}')"

# 7일 초과 백업 삭제
DELETED=$(find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -mtime +${RETENTION_DAYS} -print -delete | wc -l)
echo "오래된 백업 삭제: ${DELETED}건 (${RETENTION_DAYS}일 초과)"

# cron 등록 안내
echo ""
echo "자동 백업 설정 (cron):"
echo "  0 2 * * * cd $(pwd) && DB_PASSWORD=\$DB_PASSWORD ./deploy/backup.sh"
