#!/bin/bash
# docs/build-docs.sh -- Markdown -> PDF 변환 (pandoc 필요)
# 사용법: ./docs/build-docs.sh
# 요구사항: pandoc, xelatex, 한글 폰트 (NanumGothic 등)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
OUT_DIR="$SCRIPT_DIR/pdf"
mkdir -p "$OUT_DIR"

# CJK(한글) 폰트 설정 -- 환경변수로 오버라이드 가능
CJK_FONT="${CJK_FONT:-NanumGothic}"

echo "=== 해군 사상자 관리 시스템 -- 문서 PDF 변환 ==="
echo "출력 디렉토리: $OUT_DIR"
echo "한글 폰트: $CJK_FONT"
echo ""

# 1. 배포 절차서 + 운영자 매뉴얼 (pandoc + xelatex)
for md in deployment-guide.md operations-manual.md; do
  if [ ! -f "$SCRIPT_DIR/$md" ]; then
    echo "WARN: $md 파일 없음 -- 건너뜀"
    continue
  fi
  echo "Converting $md -> PDF..."
  pandoc "$SCRIPT_DIR/$md" \
    -o "$OUT_DIR/${md%.md}.pdf" \
    --pdf-engine=xelatex \
    -V mainfont="$CJK_FONT" \
    -V geometry:margin=2cm \
    -V fontsize=11pt \
    --toc \
    -V toc-title="목차"
  echo "  -> $OUT_DIR/${md%.md}.pdf"
done

# 2. 교육 슬라이드 (Marp CLI 우선, pandoc beamer 대체)
if [ ! -f "$SCRIPT_DIR/training-slides.md" ]; then
  echo "WARN: training-slides.md 파일 없음 -- 건너뜀"
elif command -v marp &> /dev/null; then
  echo "Converting training-slides.md -> PDF (Marp CLI)..."
  marp "$SCRIPT_DIR/training-slides.md" \
    --pdf --allow-local-files \
    -o "$OUT_DIR/training-slides.pdf"
  echo "  -> $OUT_DIR/training-slides.pdf"
else
  echo "Marp CLI 미설치 -- pandoc beamer로 대체 변환"
  pandoc "$SCRIPT_DIR/training-slides.md" \
    -t beamer \
    -o "$OUT_DIR/training-slides.pdf" \
    --pdf-engine=xelatex \
    -V mainfont="$CJK_FONT" \
    -V theme=metropolis
  echo "  -> $OUT_DIR/training-slides.pdf"
fi

echo ""
echo "=== PDF 생성 완료 ==="
ls -la "$OUT_DIR"/*.pdf 2>/dev/null || echo "생성된 PDF 없음"
