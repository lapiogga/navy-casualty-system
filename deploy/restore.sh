#!/bin/bash
set -euo pipefail
DB_USER="${DB_USER:-casualty}"
DB_NAME="${DB_NAME:-casualty}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"

if [ -z "${1:-}" ]; then
  echo "사용법: $0 <백업파일>"
  echo "예시: $0 backups/casualty_20260404_020000.sql.gz"
  ls -lt ${BACKUP_DIR:-./backups}/*.sql.gz 2>/dev/null | head -5
  exit 1
fi

BACKUP_FILE="$1"
if [ ! -f "$BACKUP_FILE" ]; then
  echo "오류: 파일을 찾을 수 없습니다: $BACKUP_FILE"
  exit 1
fi

echo "경고: 기존 데이터베이스 '$DB_NAME'의 모든 데이터가 교체됩니다."
read -p "계속하시겠습니까? (yes/no): " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
  echo "복원 취소"
  exit 0
fi

echo "복원 시작: $BACKUP_FILE -> $DB_NAME"
gunzip -c "$BACKUP_FILE" | PGPASSWORD="${DB_PASSWORD}" psql \
  -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME"

echo "복원 완료"
