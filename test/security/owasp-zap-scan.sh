#!/bin/bash
# OWASP ZAP 자동 보안 스캔 스크립트
# 전제: Docker 설치, 앱이 http://localhost:8080에서 실행 중
#
# 사용법:
#   bash test/security/owasp-zap-scan.sh [TARGET_URL]
#
# 예시:
#   bash test/security/owasp-zap-scan.sh http://localhost:8080
#   bash test/security/owasp-zap-scan.sh http://host.docker.internal:8080

set -euo pipefail

TARGET_URL="${1:-http://host.docker.internal:8080}"
REPORT_DIR="$(dirname "$0")/reports"
mkdir -p "$REPORT_DIR"

echo "=== OWASP ZAP 베이스라인 스캔 시작 ==="
echo "대상 URL: $TARGET_URL"
echo "보고서 디렉토리: $REPORT_DIR"
echo ""

docker run --rm \
  -v "$REPORT_DIR:/zap/wrk:rw" \
  zaproxy/zap-stable zap-baseline.py \
    -t "$TARGET_URL" \
    -r zap-report.html \
    -J zap-report.json \
    -l WARN \
    -I

echo ""
echo "=== ZAP 스캔 완료 ==="
echo "HTML 보고서: $REPORT_DIR/zap-report.html"
echo "JSON 보고서: $REPORT_DIR/zap-report.json"
echo ""
echo "HIGH 취약점이 없어야 합니다."
echo "보고서를 확인하여 WARN 이상의 경고를 검토하세요."
