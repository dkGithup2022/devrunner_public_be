package dev.devrunner.encryption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 이메일 암호화 유틸
 * <p>
 * - AES-256-GCM with Deterministic IV
 * - 같은 이메일 → 같은 암호문 (검색 가능)
 * - GDPR/ICO 규정 준수
 */
@Slf4j
@Component
public class EmailEncryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int GCM_IV_LENGTH = 12; // bytes

    private final SecretKeySpec secretKey;
    private final SecretKeySpec hmacKey;

    public EmailEncryptor(
            @Value("${encryption.email.secret-key}") String secretKeyHex
    ) {
        log.info("email key : {}", secretKeyHex);
        // AES-256 키 (32 bytes)
        byte[] keyBytes = hexToBytes(secretKeyHex);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Secret key must be 32 bytes (256 bits)");
        }

        this.secretKey = new SecretKeySpec(keyBytes, "AES");

        // HMAC 키 (IV 생성용 - 같은 키 재사용)
        this.hmacKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        log.info("EmailEncryptor initialized with AES-256-GCM");
    }

    /**
     * 이메일 암호화
     *
     * @param plainEmail 평문 이메일
     * @return Base64 인코딩된 암호문
     */
    public String encrypt(String plainEmail) {
        if (plainEmail == null || plainEmail.isEmpty()) {
            return plainEmail;
        }

        try {
            // 1. Deterministic IV 생성 (이메일 HMAC의 첫 12 bytes)
            byte[] iv = generateDeterministicIV(plainEmail);

            // 2. AES-GCM 암호화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] plainBytes = plainEmail.getBytes(StandardCharsets.UTF_8);
            byte[] cipherBytes = cipher.doFinal(plainBytes);

            // 3. IV + ciphertext 합쳐서 Base64 인코딩
            byte[] combined = ByteBuffer.allocate(iv.length + cipherBytes.length)
                    .put(iv)
                    .put(cipherBytes)
                    .array();

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            log.error("Failed to encrypt email", e);
            throw new RuntimeException("Email encryption failed", e);
        }
    }

    /**
     * 이메일 복호화
     *
     * @param encryptedEmail Base64 인코딩된 암호문
     * @return 평문 이메일
     */
    public String decrypt(String encryptedEmail) {
        if (encryptedEmail == null || encryptedEmail.isEmpty()) {
            return encryptedEmail;
        }

        try {
            // 1. Base64 디코딩
            byte[] combined = Base64.getDecoder().decode(encryptedEmail);

            // 2. IV와 ciphertext 분리
            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);

            byte[] cipherBytes = new byte[buffer.remaining()];
            buffer.get(cipherBytes);

            // 3. AES-GCM 복호화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainBytes = cipher.doFinal(cipherBytes);

            return new String(plainBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Failed to decrypt email", e);
            throw new RuntimeException("Email decryption failed", e);
        }
    }

    /**
     * Deterministic IV 생성
     * - 같은 이메일 → 같은 IV → 같은 암호문
     * - HMAC-SHA256(email)의 첫 12 bytes 사용
     */
    private byte[] generateDeterministicIV(String email) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(hmacKey);
            byte[] hash = hmac.doFinal(email.getBytes(StandardCharsets.UTF_8));

            // 첫 12 bytes를 IV로 사용
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(hash, 0, iv, 0, GCM_IV_LENGTH);

            return iv;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate deterministic IV", e);
        }
    }

    /**
     * Hex 문자열을 byte[]로 변환
     */
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
