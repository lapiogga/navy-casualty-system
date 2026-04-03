import { describe, it, expect } from 'vitest';
import { validateRrn } from '../utils/rrnValidation';

describe('validateRrn', () => {
  it('유효한 주민번호를 통과시킨다', () => {
    // 가중치 [2,3,4,5,6,7,8,9,2,3,4,5], mod 11 검증
    // 8 5 0 4 2 2 - 1 0 8 1 5 1 6
    // sum = 8*2 + 5*3 + 0*4 + 4*5 + 2*6 + 2*7 + 1*8 + 0*9 + 8*2 + 1*3 + 5*4 + 1*5
    //     = 16 + 15 + 0 + 20 + 12 + 14 + 8 + 0 + 16 + 3 + 20 + 5 = 129
    // check = (11 - (129 % 11)) % 10 = (11 - 8) % 10 = 3 % 10 = 3
    // 하지만 마지막 자리가 6이므로 이건 틀림. 직접 계산한 유효한 번호를 사용.

    // 직접 계산: 900101-1234567
    // digits: 9 0 0 1 0 1 1 2 3 4 5 6 7
    // sum = 9*2 + 0*3 + 0*4 + 1*5 + 0*6 + 1*7 + 1*8 + 2*9 + 3*2 + 4*3 + 5*4 + 6*5
    //     = 18 + 0 + 0 + 5 + 0 + 7 + 8 + 18 + 6 + 12 + 20 + 30 = 124
    // check = (11 - (124 % 11)) % 10 = (11 - 3) % 10 = 8 % 10 = 8
    // 마지막 자리 7 !== 8, 무효

    // 유효한 번호 직접 생성: 900101-123456X 에서 X = (11 - sum%11) % 10
    // sum = 124 (위), check = 8 -> 900101-1234568
    expect(validateRrn('900101-1234568')).toBe(true);
  });

  it('하이픈 없이도 유효한 주민번호를 통과시킨다', () => {
    expect(validateRrn('9001011234568')).toBe(true);
  });

  it('체크섬이 틀린 주민번호를 거부한다', () => {
    expect(validateRrn('900101-1234567')).toBe(false);
  });

  it('모든 자리가 0인 주민번호를 거부한다', () => {
    expect(validateRrn('000000-0000000')).toBe(false);
  });

  it('빈 문자열을 거부한다', () => {
    expect(validateRrn('')).toBe(false);
  });

  it('null을 거부한다', () => {
    expect(validateRrn(null)).toBe(false);
  });

  it('undefined를 거부한다', () => {
    expect(validateRrn(undefined)).toBe(false);
  });

  it('길이가 부족한 입력을 거부한다', () => {
    expect(validateRrn('12345')).toBe(false);
  });

  it('길이가 초과한 입력을 거부한다', () => {
    expect(validateRrn('900101-12345678')).toBe(false);
  });

  it('숫자가 아닌 문자가 포함된 입력을 거부한다', () => {
    expect(validateRrn('90010a-1234568')).toBe(false);
  });
});
