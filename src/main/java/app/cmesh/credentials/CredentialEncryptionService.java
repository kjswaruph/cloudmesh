package app.cmesh.credentials;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class CredentialEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits recommended for GCM
    private static final int GCM_TAG_LENGTH = 128; // 128 bits authentication tag
    private static final int AES_KEY_SIZE = 256; // 256 bits

    private final SecretKey encryptionKey;
    private final SecureRandom secureRandom;

    public CredentialEncryptionService(
            @Value("${credential.encryption.key}") String encryptionKeyBase64) {

        if (encryptionKeyBase64 == null || encryptionKeyBase64.isBlank()) {
            throw new IllegalStateException(
                    "Encryption key not configured. Set 'credential.encryption.key' environment variable.");
        }

        this.encryptionKey = decodeKey(encryptionKeyBase64);
        this.secureRandom = new SecureRandom();

        log.info("Credential encryption service initialized with AES-256-GCM");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            // Generate random IV for this encryption
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, parameterSpec);

            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            // Combine IV + Ciphertext + Tag into single byte array
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            // Encode as Base64 for storage
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new EncryptionException("Failed to encrypt credential data", e);
        }
    }

    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }

        try {
            return decryptWithKey(ciphertext, encryptionKey);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new EncryptionException("Failed to decrypt credential data", e);
        }
    }

    private String decryptWithKey(String ciphertext, SecretKey key) throws Exception {
        // Decode from Base64
        byte[] decoded = Base64.getDecoder().decode(ciphertext);

        // Extract IV and ciphertext
        ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);
        byte[] ciphertextBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(ciphertextBytes);

        // Initialize cipher
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        // Decrypt and verify authentication tag
        byte[] plaintext = cipher.doFinal(ciphertextBytes);

        return new String(plaintext);
    }

    public boolean isEncrypted(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            // Encrypted data should be at least IV length + some ciphertext + tag
            return decoded.length > GCM_IV_LENGTH + 16;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String generateNewKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_SIZE);
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("Failed to generate encryption key", e);
        }
    }

    private SecretKey decodeKey(String base64Key) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(base64Key);
            if (decodedKey.length != 32) { // 256 bits = 32 bytes
                throw new IllegalArgumentException(
                        "Invalid key length: " + (decodedKey.length * 8) + " bits. Expected 256 bits.");
            }
            return new SecretKeySpec(decodedKey, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Invalid encryption key format", e);
        }
    }

}
