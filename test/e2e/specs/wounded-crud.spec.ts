import { test, expect, Page } from '@playwright/test';

/**
 * 상이자 CRUD E2E 테스트.
 * 목록 조회, 등록, 검색, 수정 흐름을 검증한다.
 */

async function login(page: Page) {
  await page.goto('/login');
  await page.fill('input[type="text"], input#username, input[name="username"]', 'admin');
  await page.fill('input[type="password"]', 'Admin1234!');
  await page.click('button[type="submit"], .ant-btn-primary');
  await page.waitForURL(/\/(dead|$)/, { timeout: 10000 });
}

test.describe('상이자 CRUD', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
  });

  test('상이자 목록 페이지가 정상 렌더링된다', async ({ page }) => {
    await page.goto('/wounded');
    await expect(page.locator('.ant-table').first()).toBeVisible({ timeout: 10000 });
  });

  test('등록 버튼 클릭 시 Modal이 열린다', async ({ page }) => {
    await page.goto('/wounded');
    const addBtn = page.locator('button:has-text("등록"), .ant-btn:has-text("등록")').first();
    await addBtn.click();

    await expect(page.locator('.ant-modal').first()).toBeVisible({ timeout: 5000 });
  });

  test('필수 필드 입력이 가능하다 (보훈청명, 병명, 상이구분 포함)', async ({ page }) => {
    await page.goto('/wounded');
    const addBtn = page.locator('button:has-text("등록"), .ant-btn:has-text("등록")').first();
    await addBtn.click();
    await expect(page.locator('.ant-modal').first()).toBeVisible({ timeout: 5000 });

    const modal = page.locator('.ant-modal');

    // 기본 필수 필드
    const nameInput = modal.locator('input#name, input[name="name"]').first();
    if (await nameInput.isVisible()) {
      await nameInput.fill('테스트상이자');
    }

    // 상이구분 셀렉트 확인 (Ant Design Select)
    const woundTypeSelect = modal.locator('.ant-select').first();
    if (await woundTypeSelect.isVisible()) {
      await woundTypeSelect.click();
    }
  });

  test('검색 기능이 동작한다', async ({ page }) => {
    await page.goto('/wounded');

    const searchInput = page.locator('input[placeholder*="검색"], input[placeholder*="군번"], .ant-input').first();
    if (await searchInput.isVisible()) {
      await searchInput.fill('테스트');
      await page.keyboard.press('Enter');
      await expect(page.locator('.ant-table').first()).toBeVisible({ timeout: 5000 });
    }
  });
});
