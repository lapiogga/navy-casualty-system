import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './specs',
  baseURL: process.env.BASE_URL || 'http://localhost:8080',
  timeout: 30000,
  retries: 1,
  use: {
    headless: true,
    screenshot: 'only-on-failure',
    trace: 'on-first-retry',
  },
  reporter: [
    ['html', { outputFolder: 'report' }],
    ['list'],
  ],
});
