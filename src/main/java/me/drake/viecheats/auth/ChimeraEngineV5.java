package me.drake.viecheats.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class ChimeraEngineV5 {
    public static final String PROTOCOL = "CHIMERA-PROTECT";
    private static final char[] DNA_MAP;
    private static final byte[] REV_DNA_MAP;
    private static final int GCM_NONCE_BYTES = 12;
    private static final int GCM_TAG_BYTES = 16;
    private static final int GCM_TAG_BITS = 128;
    private static final int PBKDF2_ITERATIONS = 100000;
    private static final int AES_KEY_BYTES = 32;
    private static final int SALT_BYTES = 16;
    private final byte[] masterKey;
    private final SecureRandom rng;

    public ChimeraEngineV5(byte[] masterKey) {
        if ((masterKey == null) || (masterKey.length == 0)) {
            throw new IllegalArgumentException("masterKey cannot be null or empty");
        }
        this.masterKey = ((byte[]) masterKey.clone());
        this.rng = new SecureRandom();
    }

    public static byte[] deriveMasterKey(String password) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex3) {
            NoSuchAlgorithmException e = ex3;
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static byte[] deriveMasterKey(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, 32);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return f.generateSecret(spec).getEncoded();
        } catch (Exception ex5) {
            Exception e = ex5;
            throw new RuntimeException("PBKDF2 failed", e);
        }
    }

    public Map encode(String rawStr) {
        if (rawStr == null) throw new IllegalArgumentException("rawStr cannot be null");
        byte[] salt = randomBytes(16);
        byte[] nonce = randomBytes(12);
        byte[] key = deriveKey(salt);
        byte[] plaintext = rawStr.getBytes(StandardCharsets.UTF_8);
        try {
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, aesKey(key), gcmSpec);
            byte[] encryptedPayload = cipher.doFinal(plaintext);
            int encLen = (encryptedPayload.length - 16);
            byte[] actualCiphertext = new byte[encLen];
            byte[] authTag = new byte[16];
            System.arraycopy(encryptedPayload, 0, actualCiphertext, 0, encLen);
            System.arraycopy(encryptedPayload, encLen, authTag, 0, 16);
            byte[] blob = ByteBuffer.allocate((28 + encLen)).put(nonce).put(authTag).put(actualCiphertext).array();
            Map result = new LinkedHashMap();
            result.put("sequence", bytesToDna(blob));
            result.put("locus", bytesToHex(salt));
            result.put("protocol", "CHIMERA-PROTECT");
            result.put("version", 5);
            return result;
        } catch (Exception ex27) {
            Exception e = ex27;
            throw new RuntimeException("Chimera encode failed", e);
        }
    }

    public String decode(String dnaSequence, String saltHex) {
        byte[] result = decodeBytes(dnaSequence, saltHex);
        return new String(result, StandardCharsets.UTF_8);
    }

    public byte[] decodeBytes(String dnaSequence, String saltHex) {
        if ((dnaSequence == null) || (saltHex == null)) {
            throw new IllegalArgumentException("dnaSequence and saltHex cannot be null");
        }
        byte[] salt = hexToBytes(saltHex);
        byte[] blob = dnaToBytes(dnaSequence);
        if (blob.length < 28) throw new IllegalArgumentException("Chimera blob too short");
        ByteBuffer buf = ByteBuffer.wrap(blob);
        byte[] nonce = new byte[12];
        byte[] authTag = new byte[16];
        byte[] ciphertext = new byte[((blob.length - 12) - 16)];
        buf.get(nonce);
        buf.get(authTag);
        buf.get(ciphertext);
        try {
            byte[] key = deriveKey(salt);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(2, aesKey(key), gcmSpec);
            byte[] ctWithTag = ByteBuffer.allocate((ciphertext.length + 16)).put(ciphertext).put(authTag).array();
            return cipher.doFinal(ctWithTag);
        } catch (Exception ex23) {
            Exception e = ex23;
            throw new RuntimeException(("Chimera decode failed: " + e.getMessage()), e);
        }
    }

    public String encodeToJson(String rawStr) {
        Map pkt = encode(rawStr);
        String json = new GsonBuilder().disableHtmlEscaping().create().toJson(pkt);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public String decodeFromJson(String base64Json) {
        String json = new String(Base64.getUrlDecoder().decode(base64Json), StandardCharsets.UTF_8);
        Map pkt = ((Map) new Gson().fromJson(json, Map.class));
        return decode(((String) pkt.get("sequence")), ((String) pkt.get("locus")));
    }

    private byte[] deriveKey(byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(toChars(this.masterKey), salt, 100000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] derived = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return derived;
        } catch (Exception ex7) {
            Exception e = ex7;
            throw new RuntimeException("PBKDF2 key derivation failed", e);
        }
    }

    private char[] toChars(byte[] bytes) {
        char[] chars = new char[bytes.length];
        int i = 0;
        while (i < bytes.length) {
            chars[i] = ((char) (bytes[i] & 255));
            i++;
        }
        return chars;
    }

    private String bytesToDna(byte[] data) {
        char[] out = new char[(data.length * 4)];
        int i = 0;
        while (i < data.length) {
            byte b = data[i];
            out[((i * 4) + 0)] = DNA_MAP[((b >> 0) & 3)];
            out[((i * 4) + 1)] = DNA_MAP[((b >> 2) & 3)];
            out[((i * 4) + 2)] = DNA_MAP[((b >> 4) & 3)];
            out[((i * 4) + 3)] = DNA_MAP[((b >> 6) & 3)];
            i++;
        }
        return new String(out);
    }

    private byte[] dnaToBytes(String dna) {
        int len = dna.length();
        if ((len % 4) != 0) throw new IllegalArgumentException("DNA sequence length must be multiple of 4");
        byte[] out = new byte[(len / 4)];
        int i = 0;
        while (i < len) {
            byte b0 = REV_DNA_MAP[(dna.charAt((i + 0)) & 255)];
            byte b1 = REV_DNA_MAP[(dna.charAt((i + 1)) & 255)];
            byte b2 = REV_DNA_MAP[(dna.charAt((i + 2)) & 255)];
            byte b3 = REV_DNA_MAP[(dna.charAt((i + 3)) & 255)];
            out[(i / 4)] = ((byte) ((((b0 << 0) | (b1 << 2)) | (b2 << 4)) | (b3 << 6)));
            i += 4;
        }
        return out;
    }

    private byte[] randomBytes(int len) {
        byte[] b = new byte[len];
        this.rng.nextBytes(b);
        return b;
    }

    private static SecretKey aesKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder((bytes.length * 2));
        Object var2 = bytes;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            byte b = var2[var4];
            var tmp0 = new Object[1];
            tmp0[0] = (b & 255);
            sb.append(String.format("%02x", tmp0));
            var4++;
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] out = new byte[(len / 2)];
        int i = 0;
        while (i < len) {
            out[(i / 2)] = ((byte) Integer.parseInt(hex.substring(i, (i + 2)), 16));
            i += 2;
        }
        return out;
    }

    static {
        var tmp0 = new char[4];
        tmp0[0] = 65;
        tmp0[1] = 84;
        tmp0[2] = 71;
        tmp0[3] = 67;
        DNA_MAP = tmp0;
        REV_DNA_MAP = new byte[256];
        REV_DNA_MAP[65] = 0;
        REV_DNA_MAP[84] = 1;
        REV_DNA_MAP[71] = 2;
        REV_DNA_MAP[67] = 3;
    }

}
