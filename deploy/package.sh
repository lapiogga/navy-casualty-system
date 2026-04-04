#!/bin/bash
set -euo pipefail
VERSION="${1:-1.0.0}"
echo "=== 해군 사상자 관리 시스템 v${VERSION} 패키지 빌드 ==="

# 1. Docker 이미지 빌드
docker compose -f docker-compose.yml -f docker-compose.prod.yml build

# 2. 이미지 태깅
docker tag navy-casualty-app:latest navy-casualty-app:v${VERSION}

# 3. Docker 이미지 tar 아카이브
echo "Docker 이미지 저장 중..."
docker save navy-casualty-app:v${VERSION} postgres:16-alpine | gzip > navy-casualty-images-v${VERSION}.tar.gz

# 4. 배포 패키지 조립
echo "배포 패키지 조립 중..."
tar czf navy-casualty-deploy-v${VERSION}.tar.gz \
  navy-casualty-images-v${VERSION}.tar.gz \
  docker-compose.yml \
  docker-compose.prod.yml \
  deploy/deploy.sh \
  deploy/generate-cert.sh \
  deploy/backup.sh \
  deploy/restore.sh \
  .env.example

rm navy-casualty-images-v${VERSION}.tar.gz
echo "=== 패키지 완료: navy-casualty-deploy-v${VERSION}.tar.gz ==="
ls -lh navy-casualty-deploy-v${VERSION}.tar.gz
