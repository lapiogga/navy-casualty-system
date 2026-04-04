#!/bin/bash
set -euo pipefail
echo "=== 해군 사상자 관리 시스템 배포 ==="

# 1. Docker 이미지 로드
IMAGES_TAR=$(ls navy-casualty-images-*.tar.gz 2>/dev/null | head -1)
if [ -z "$IMAGES_TAR" ]; then
  echo "오류: 이미지 파일(navy-casualty-images-*.tar.gz)을 찾을 수 없습니다."
  exit 1
fi
echo "이미지 로드 중: $IMAGES_TAR"
docker load < "$IMAGES_TAR"

# 2. 환경변수 파일 확인
if [ ! -f .env ]; then
  echo "오류: .env 파일이 없습니다. .env.example을 복사하여 설정하세요."
  echo "  cp .env.example .env && vi .env"
  exit 1
fi

# 3. TLS 인증서 확인 (선택)
if [ ! -d certs ]; then
  echo "경고: certs/ 디렉토리가 없습니다. TLS 없이 HTTP로 실행됩니다."
  echo "  TLS 적용: ./deploy/generate-cert.sh 실행 후 .env에서 SERVER_SSL_ENABLED=true 설정"
fi

# 4. Docker Compose 실행
echo "서비스 시작 중..."
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 5. 헬스체크 대기
echo "서비스 상태 확인 중 (최대 120초)..."
for i in $(seq 1 24); do
  if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "서비스가 정상 시작되었습니다."
    docker compose ps
    exit 0
  fi
  sleep 5
done
echo "경고: 서비스가 120초 내에 시작되지 않았습니다. 로그를 확인하세요:"
echo "  docker compose logs app"
exit 1
