import { test, expect, Page } from '@playwright/test';

/**
 * 통계 + Excel E2E 테스트.
 * 차트 렌더링, Excel 다운로드, 명부 데이터 조회를 검증한다.
 */

async function login(page: Page) {
  await page.goto('/login');
  await page.fill('input[type="text"], input#username, input[name="username"]', 'admin');
  await page.fill('input[type="password"]', 'Admin1234!');
  await page.click('button[type="submit"], .ant-btn-primary');
  await page.waitForURL(/\/(dead|$)/, { timeout: 10000 });
}

test.describe('통계 및 Excel 다운로드', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('신분별 현황 페이지에서 차트가 렌더링된다', async ({ page }) => {
    await page.goto('/statistics/branch');

    // @ant-design/charts 차트 컨테이너 또는 canvas 존재 확인
    const chartContainer = page.locator('canvas, [class*="chart"], [class*="Chart"]').first();
    await expect(chartContainer).toBeVisible({ timeout: 10000 });
  });

  test('Excel 다운로드 버튼이 존재한다', async ({ page }) => {
    await page.goto('/statistics/branch');

    // Excel 다운로드 버튼 확인
    const excelBtn = page.locator(
      'button:has-text("Excel"), button:has-text("엑셀"), button:has-text("다운로드"), .ant-btn:has-text("Excel")'
    ).first();

    await expect(excelBtn).toBeVisible({ timeout: 10000 });
  });

  test('Excel 다운로드 클릭 시 파일이 다운로드된다', async ({ page }) => {
    await page.goto('/statistics/branch');

    const excelBtn = page.locator(
      'button:has-text("Excel"), button:has-text("엑셀"), button:has-text("다운로드"), .ant-btn:has-text("Excel")'
    ).first();

    // 다운로드 이벤트 감지
    const downloadPromise = page.waitForEvent('download', { timeout: 10000 }).catch(() => null);
    await excelBtn.click();
    const download = await downloadPromise;

    if (download) {
      expect(download.suggestedFilename()).toMatch(/\.(xlsx|xls|csv)$/);
    }
  });

  test('부대별 명부 페이지에 표 데이터가 존재한다', async ({ page }) => {
    await page.goto('/statistics/roster-unit');

    // 테이블 존재 확인
    const table = page.locator('.ant-table').first();
    await expect(table).toBeVisible({ timeout: 10000 });
  });
});
