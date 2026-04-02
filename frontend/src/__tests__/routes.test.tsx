import { describe, it, expect } from 'vitest';
import router from '../routes';

describe('라우트 설정', () => {
  it('루트 라우트가 존재한다', () => {
    const rootRoute = router.routes[0];
    expect(rootRoute.path).toBe('/');
  });

  it('6개 하위 라우트가 존재한다', () => {
    const rootRoute = router.routes[0];
    const childPaths = rootRoute.children?.map((c: { path?: string }) => c.path).filter(Boolean);
    expect(childPaths).toEqual(
      expect.arrayContaining(['dead', 'wounded', 'review', 'document', 'statistics', 'admin'])
    );
  });

  it('인덱스 라우트가 /dead로 리다이렉트한다', () => {
    const rootRoute = router.routes[0];
    const indexRoute = rootRoute.children?.find((c: { index?: boolean }) => c.index);
    expect(indexRoute).toBeDefined();
  });

  it('/login 라우트가 존재한다', () => {
    const loginRoute = router.routes.find((r: { path?: string }) => r.path === '/login');
    expect(loginRoute).toBeDefined();
  });
});
