/**
 * 주민등록번호 체크섬 검증
 * 가중치 [2,3,4,5,6,7,8,9,2,3,4,5], mod 11 알고리즘
 */
export function validateRrn(rrn: string | null | undefined): boolean {
  if (!rrn) return false;
  const digits = rrn.replace('-', '');
  if (digits.length !== 13 || !/^\d{13}$/.test(digits)) return false;

  const weights = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5];
  let sum = 0;
  for (let i = 0; i < 12; i++) {
    sum += Number(digits[i]) * weights[i];
  }
  const checkDigit = (11 - (sum % 11)) % 10;
  return checkDigit === Number(digits[12]);
}
