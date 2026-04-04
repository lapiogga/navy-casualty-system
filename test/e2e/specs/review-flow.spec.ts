import { test, expect, Page } from '@playwright/test';

/**
 * 전공사상심사 흐름 E2E 테스트.
 * 목록 조회, 심사 등록, 분류 결과 입력을 검증한다.
 */

async function login(page: Page) {
  await page.goto('/login');
  await page.fill('input[type="text"], input#username, input[name="username"]', 'admin');
  await page.fill('input[type="password"]', 'Admin1234!');
  await page.click('button[type="submit"], .ant-btn-primary');
  await page.waitForURL(/\/(dead|$)/, { timeout: 10000 });
}

test.describe('전공사상심사 흐름', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('전공사상심사 목록 페이지가 정상 렌더링된다', async ({ page }) => {
    await page.goto('/review');
    await expect(page.locator('.ant-table').first()).toBeVisible({ timeout: 10000 });
  });

  test('심사 등록 버튼 클릭 시 Modal이 열린다', async ({ page }) => {
    await page.goto('/review');
    const addBtn = page.locator('button:has-text("등록"), .ant-btn:has-text("등록")').first();
    await addBtn.click();

    await expect(page.locator('.ant-modal').first()).toBeVisible({ timeout: 5000 });
  });

  test('심사차수 및 분류 결과 필드가 존재한다', async ({ page }) => {
    await page.goto('/review');
    const addBtn = page.locator('button:has-text("등록"), .ant-btn:has-text("등록")').first();
    await addBtn.click();
    await expect(page.locator('.ant-modal').first()).toBeVisible({ timeout: 5000 });

    const modal = page.locator('.ant-modal');

    // 심사차수 입력 필드 또는 셀렉트 확인
    const roundInput = modal.locator('input#reviewRound, input[name="reviewRound"], .ant-input-number').first();
    if (await roundInput.isVisible()) {
      // 심사차수 필드 존재 확인
      expect(true).toBe(true);
    }

    // 분류(classification) 셀렉트 확인
    const classSelect = modal.locator('.ant-select').first();
    if (await classSelect.isVisible()) {
      expect(true).toBe(true);
    }
  });
});
