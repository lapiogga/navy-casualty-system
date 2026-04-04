#!/bin/bash
set -euo pipefail
CERT_DIR="./certs"
KEYSTORE="$CERT_DIR/keystore.p12"
ALIAS="navy-casualty"
VALIDITY=365

mkdir -p "$CERT_DIR"

read -sp "키스토어 비밀번호 입력: " KS_PASSWORD
echo

# 자체 서명 인증서 + PKCS12 키스토어 생성
keytool -genkeypair \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore "$KEYSTORE" \
  -storepass "$KS_PASSWORD" \
  -validity "$VALIDITY" \
  -dname "CN=navy-casualty,OU=Navy,O=ROK Navy,L=Gyeryong,ST=Chungnam,C=KR"

echo "인증서 생성 완료: $KEYSTORE"
echo ""
echo "다음 단계:"
echo "1. .env 파일에 다음 설정 추가:"
echo "   SERVER_SSL_ENABLED=true"
echo "   SSL_KEYSTORE_PASSWORD=$KS_PASSWORD"
echo "2. 실 운영 시에는 CA 발급 인증서로 교체하세요."
