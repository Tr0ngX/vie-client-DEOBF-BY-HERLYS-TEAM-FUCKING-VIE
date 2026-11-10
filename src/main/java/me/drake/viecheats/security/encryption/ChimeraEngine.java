package me.drake.viecheats.security.encryption;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Purified ChimeraEngine: DNA-Encoded PBKDF2-HMAC-SHA256 + AES-256-GCM Cipher Manager.
 * Obfuscated dead byte[] OBFUSCATED_FALLBACK_KEY and XOR_KEY artifacts have been completely removed,
 * and the decrypted fallback key literal is permanently inlined.
 */
public class ChimeraEngine {
    private static final String CIPHER_ALGO = "AES/GCM/NoPadding";
    private static final String KDF_ALGO = "PBKDF2WithHmacSHA256";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_NONCE_BYTES = 12;
    private static final int SALT_BYTES = 16;
    private static final int KDF_ITERATIONS = 100000;
    private static final int KEY_BITS = 256;
    private static final String FALLBACK_KEY = "Chimera_Protect_Discord_xduyhoangg_2026";
    private static final char[] DNA_MAP = new char[]{'A', 'T', 'G', 'C'};

    private static String decodeFallbackKey() {
        return FALLBACK_KEY;
    }

    private static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        String effectivePass = (password != null && !password.isEmpty())
                ? password + decodeFallbackKey()
                : decodeFallbackKey();
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KDF_ALGO);
        KeySpec spec = new PBEKeySpec(effectivePass.toCharArray(), salt, KDF_ITERATIONS, KEY_BITS);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private static String bytesToDna(byte[] data) {
        StringBuilder dna = new StringBuilder(data.length * 4);
        for (byte b : data) {
            int val = b & 0xFF;
            for (int i = 0; i < 4; i++) {
                int twoBits = (val >> (i * 2)) & 0x03;
                dna.append(DNA_MAP[twoBits]);
            }
        }
        return dna.toString();
    }

    private static byte[] dnaToBytes(String dna) {
        if (dna == null || dna.length() % 4 != 0) {
            throw new IllegalArgumentException("DNA Sequence Corrupted (Invalid Length)");
        }
        byte[] data = new byte[dna.length() / 4];
        for (int i = 0; i < data.length; i++) {
            int byteVal = 0;
            for (int j = 0; j < 4; j++) {
                char c = dna.charAt(i * 4 + j);
                int twoBits = switch (c) {
                    case 'A' -> 0;
                    case 'T' -> 1;
                    case 'G' -> 2;
                    case 'C' -> 3;
                    default -> throw new IllegalArgumentException("Mutated DNA Base Detected: " + c);
                };
                byteVal |= (twoBits << (j * 2));
            }
            data[i] = (byte) byteVal;
        }
        return data;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public static GenomePayload synthesize(String plainText, String customKey) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_BYTES];
            random.nextBytes(salt);
            byte[] nonce = new byte[GCM_NONCE_BYTES];
            random.nextBytes(nonce);

            SecretKey key = deriveKey(customKey, salt);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[nonce.length + cipherText.length];
            System.arraycopy(nonce, 0, combined, 0, nonce.length);
            System.arraycopy(cipherText, 0, combined, nonce.length, cipherText.length);

            return new GenomePayload(bytesToDna(combined), bytesToHex(salt));
        } catch (Exception e) {
            throw new RuntimeException("Synthesis Failed", e);
        }
    }

    public static String sequence(GenomePayload payload, String customKey) throws Exception {
        if (payload == null || payload.dna == null || payload.locus == null) {
            throw new IllegalArgumentException("Empty Genome Payload");
        }
        byte[] salt = hexToBytes(payload.locus.trim());
        byte[] combined = dnaToBytes(payload.dna.trim().toUpperCase());
        if (combined.length < GCM_NONCE_BYTES) {
            throw new IllegalArgumentException("DNA Sequence Too Short");
        }

        byte[] nonce = new byte[GCM_NONCE_BYTES];
        byte[] cipherText = new byte[combined.length - GCM_NONCE_BYTES];
        System.arraycopy(combined, 0, nonce, 0, GCM_NONCE_BYTES);
        System.arraycopy(combined, GCM_NONCE_BYTES, cipherText, 0, cipherText.length);

        SecretKey key = deriveKey(customKey, salt);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, nonce);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] plainBytes = cipher.doFinal(cipherText);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    public static class GenomePayload {
        public final String dna;
        public final String locus;

        public GenomePayload(String dna, String locus) {
            this.dna = dna;
            this.locus = locus;
        }
    }
}
