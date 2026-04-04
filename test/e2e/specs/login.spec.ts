import { test, expect } from '@playwright/test';

/**
 * 로그인 페이지 E2E 테스트.
 * 로그인 성공/실패, 로그아웃 흐름을 검증한다.
 */
test.describe('로그인 흐름', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  test('로그인 페이지가 정상 렌더링된다', async ({ page }) => {
    await expect(page.locator('input[type="text"], input#username, input[name="username"]').first()).toBeVisible();
    await expect(page.locator('input[type="password"]').first()).toBeVisible();
  });

  test('유효한 자격증명으로 로그인 성공 시 리다이렉트된다', async ({ page }) => {
    await page.fill('input[type="text"], input#username, input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'Admin1234!');
    await page.click('button[type="submit"], .ant-btn-primary');

    // 로그인 성공 후 /dead 또는 메인 페이지로 이동
    await page.waitForURL(/\/(dead|$)/, { timeout: 10000 });
    expect(page.url()).not.toContain('/login');
  });

  test('잘못된 비밀번호 입력 시 에러 메시지가 표시된다', async ({ page }) => {
    await page.fill('input[type="text"], input#username, input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'wrong');
    await page.click('button[type="submit"], .ant-btn-primary');

    // 에러 메시지 또는 알림 표시
    await expect(
      page.locator('.ant-message-error, .ant-alert-error, .ant-form-item-explain-error').first()
    ).toBeVisible({ timeout: 5000 });
  });

  test('로그아웃 후 로그인 페이지로 리다이렉트된다', async ({ page }) => {
    // 먼저 로그인
    await page.fill('input[type="text"], input#username, input[name="username"]', 'admin');
    await page.fill('input[type="password"]', 'Admin1234!');
    await page.click('button[type="submit"], .ant-btn-primary');
    await page.waitForURL(/\/(dead|$)/, { timeout: 10000 });

    // 로그아웃 버튼 클릭
    const logoutBtn = page.locator('text=로그아웃, button:has-text("로그아웃")').first();
    if (await logoutBtn.isVisible()) {
      await logoutBtn.click();
    }

    // 로그인 페이지로 리다이렉트
    await page.waitForURL(/\/login/, { timeout: 10000 });
  });
});
