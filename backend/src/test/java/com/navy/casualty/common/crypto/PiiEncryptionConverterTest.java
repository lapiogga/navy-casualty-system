package com.navy.casualty.common.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PiiEncryptionConverter 단위 테스트.
 * 테스트용 키를 직접 주입하여 환경변수 의존 없이 테스트한다.
 */
class PiiEncryptionConverterTest {

    private PiiEncryptionConverter converter;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // AES-256 테스트용 키 생성
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        String testKey = Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());
        converter = new PiiEncryptionConverter(testKey);
    }

    @Test
    @DisplayName("주민번호 암호화 후 복호화하면 원본이 복원된다")
    void encryptAndDecryptRoundTrip() {
        String original = "900101-1234567";

        String encrypted = converter.convertToDatabaseColumn(original);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    @DisplayName("암호화된 값은 원본과 다르고 Base64 형식이다")
    void encryptedValueIsDifferentFromOriginal() {
        String original = "900101-1234567";

        String encrypted = converter.convertToDatabaseColumn(original);

        assertThat(encrypted).isNotEqualTo(original);
        assertThat(encrypted).isNotBlank();
        // Base64 디코딩이 가능해야 한다
        assertThat(Base64.getDecoder().decode(encrypted)).isNotEmpty();
    }

    @Test
    @DisplayName("같은 평문을 2번 암호화하면 다른 암호문이 생성된다 (IV 랜덤)")
    void sameInputProducesDifferentCiphertext() {
        String original = "900101-1234567";

        String encrypted1 = converter.convertToDatabaseColumn(original);
        String encrypted2 = converter.convertToDatabaseColumn(original);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    @DisplayName("null 입력 시 encrypt는 null을 반환한다")
    void encryptNullReturnsNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    @DisplayName("null 입력 시 decrypt는 null을 반환한다")
    void decryptNullReturnsNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("PII_ENCRYPTION_KEY 미설정 시 기본 생성자에서 IllegalStateException 발생")
    void missingKeyThrowsException() {
        assertThatThrownBy(() -> new PiiEncryptionConverter(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PII_ENCRYPTION_KEY");
    }
}
