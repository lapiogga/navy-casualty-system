import axios from 'axios';
import router from '../routes';

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

// 세션 만료 시 로그인 페이지로 리다이렉트 (/login 경로에서는 무한 루프 방지)
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      if (window.location.pathname !== '/login') {
        router.navigate('/login');
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
