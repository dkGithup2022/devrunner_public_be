package dev.devrunner.encryption;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * EmailEncryptor 단위 테스트
 */
@Slf4j
class EmailEncryptorTest {

    private EmailEncryptor encryptor;

    // 테스트용 AES-256 키 (64자 hex)
    private static final String TEST_KEY = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @BeforeEach
    void setUp() {
        encryptor = new EmailEncryptor(TEST_KEY);
    }

    @Test
    void encrypt_decrypt_성공() {
        // given
        String plainEmail = "breakin.dev@gmail.com";

        // when
        String encrypted = encryptor.encrypt(plainEmail);
        String decrypted = encryptor.decrypt(encrypted);

        // then
        assertThat(encrypted).isNotEqualTo(plainEmail);
        assertThat(decrypted).isEqualTo(plainEmail);
    }

    @Test
    void encrypt_deterministic_같은_이메일은_같은_암호문() {
        // given
        String plainEmail = "user@example.com";

        // when
        String encrypted1 = encryptor.encrypt(plainEmail);
        String encrypted2 = encryptor.encrypt(plainEmail);

        // then
        assertThat(encrypted1).isEqualTo(encrypted2);
    }

    @Test
    void encrypt_다른_이메일은_다른_암호문() {
        // given
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        // when
        String encrypted1 = encryptor.encrypt(email1);
        String encrypted2 = encryptor.encrypt(email2);

        // then
        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    void encrypt_null_처리() {
        // when
        String encrypted = encryptor.encrypt(null);

        // then
        assertThat(encrypted).isNull();
    }

    @Test
    void encrypt_empty_처리() {
        // when
        String encrypted = encryptor.encrypt("");

        // then
        assertThat(encrypted).isEmpty();
    }

    @Test
    void decrypt_null_처리() {
        // when
        String decrypted = encryptor.decrypt(null);

        // then
        assertThat(decrypted).isNull();
    }

    @Test
    void decrypt_empty_처리() {
        // when
        String decrypted = encryptor.decrypt("");

        // then
        assertThat(decrypted).isEmpty();
    }

    @Test
    void encrypt_긴_이메일_처리() {
        // given - RFC 5321 최대 길이에 가까운 이메일
        String longEmail = "very.long.email.address.with.many.dots.and.subdomains@subdomain.company.example.com";

        // when
        String encrypted = encryptor.encrypt(longEmail);
        String decrypted = encryptor.decrypt(encrypted);

        // then
        assertThat(decrypted).isEqualTo(longEmail);
        assertThat(encrypted.length()).isLessThan(512); // VARCHAR(512) 제한 확인
    }

    @Test
    void encrypt_한글_포함_이메일() {
        // given
        String emailWithKorean = "사용자@example.com";

        // when
        String encrypted = encryptor.encrypt(emailWithKorean);
        String decrypted = encryptor.decrypt(encrypted);

        // then
        assertThat(decrypted).isEqualTo(emailWithKorean);
    }

    @Test
    void encrypt_특수문자_포함_이메일() {
        // given
        String emailWithSpecialChars = "user+tag@sub-domain.example.com";

        // when
        String encrypted = encryptor.encrypt(emailWithSpecialChars);
        String decrypted = encryptor.decrypt(encrypted);

        // then
        assertThat(decrypted).isEqualTo(emailWithSpecialChars);
    }

    @Test
    void decrypt_잘못된_암호문_예외발생() {
        // given
        String invalidCiphertext = "invalid-base64-string!!!";

        // when & then
        assertThrows(RuntimeException.class, () -> {
            encryptor.decrypt(invalidCiphertext);
        });
    }

    @Test
    void constructor_잘못된_키_길이_예외발생() {
        // given - 32 bytes가 아닌 키
        String shortKey = "0123456789abcdef";

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            new EmailEncryptor(shortKey);
        });
    }

    /**
     * 다양한 이메일 케이스에 대한 암호화/복호화 왕복 테스트
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("provideEmailTestCases")
    void encrypt_decrypt_왕복_테스트_다양한_케이스(String email) {
        // when
        String encrypted = encryptor.encrypt(email);
        String decrypted = encryptor.decrypt(encrypted);

        log.info("encrypted -> decrypted -> original : {} -> {} -> {}", encrypted, decrypted, email);
        // then
        assertThat(decrypted).isEqualTo(email);
        assertThat(encrypted).isNotEqualTo(email); // 암호화 되었는지 확인
    }

    /**
     * 테스트 케이스 제공
     */
    private static Stream<String> provideEmailTestCases() {
        return Stream.of(
                // 1. 기본 케이스
                "user@example.com",
                "admin@example.com",

                // 2. 다양한 도메인
                "user@gmail.com",
                "user@yahoo.com",
                "user@outlook.com",
                "user@naver.com",
                "user@kakao.com",

                // 3. Gmail alias (+ 기호)
                "user+tag@gmail.com",
                "user+test+123@example.com",

                // 4. 숫자 포함
                "user123@example.com",
                "123user@example.com",
                "user123test456@example.com",

                // 5. 언더스코어, 하이픈 포함
                "user_name@example.com",
                "user-name@example.com",
                "user_name-test@example.com",

                // 6. 점(.) 포함
                "first.last@example.com",
                "first.middle.last@example.com",
                "user.name.with.dots@example.com",

                // 7. 서브도메인
                "user@mail.example.com",
                "user@subdomain.company.example.com",
                "user@deep.sub.domain.example.com",

                // 8. 대소문자 혼합
                "User@Example.com",
                "USER@EXAMPLE.COM",
                "UsEr@ExAmPlE.CoM",

                // 9. 짧은 이메일
                "a@b.co",
                "x@y.z",

                // 10. 긴 로컬 파트
                "very.long.local.part.with.many.segments@example.com",
                "user.with.very.long.name.for.testing@example.com",

                // 11. 긴 도메인
                "user@very-long-domain-name-for-testing-purposes.example.com",
                "user@subdomain.with.long.name.example.com",

                // 12. 특수 문자 조합
                "user+tag-name@sub-domain.example.com",
                "first.last+tag@mail.example.com",

                // 13. 한글 포함 (국제화 이메일)
                "사용자@example.com",
                "테스트@도메인.com",
                "관리자@회사.한국",

                // 14. 일본어 포함
                "ユーザー@example.com",
                "テスト@example.jp",

                // 15. 중국어 포함
                "用户@example.com",
                "测试@example.cn",

                // 16. RFC 5321 최대 길이에 가까운 이메일
                "very.long.email.address.with.many.dots.and.segments.for.testing.maximum.length@subdomain.company.example.com"
        );
    }
}
