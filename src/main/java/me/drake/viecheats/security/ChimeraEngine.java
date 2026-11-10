package me.drake.viecheats.security;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class ChimeraEngine {
    private static final char[] DNA_MAP;
    private static final int[] DNA_REV_MAP;
    public static final String PROTOCOL = "CHIMERA-PROTECT";
    private static final int SALT_BYTES = 16;
    private static final int NONCE_BYTES = 16;
    private static final int TAG_BYTES = 16;
    private static final int KDF_ITERATIONS = 100000;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RNG;
    private final String masterKey;

    public static String decodeFallbackKey() {
        return "Chimera_Protect_Discord_xduyhoangg_2026";
    }

    public ChimeraEngine(String masterKey) {
        if ((masterKey == null) || (masterKey.isBlank())) {
            throw new IllegalArgumentException("[Chimera] masterKey must not be blank.");
        }
        this.masterKey = masterKey;
    }

    public ChimeraEngine.GenomePacket encode(String plainText) {
        try {
            byte[] salt = new byte[16];
            RNG.nextBytes(salt);
            SecretKey key = deriveKey(salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, key);
            byte[] nonce = cipher.getIV();
            byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] actualCipher = Arrays.copyOf(ciphertext, (ciphertext.length - 16));
            byte[] authTag = Arrays.copyOfRange(ciphertext, (ciphertext.length - 16), ciphertext.length);
            byte[] noncePadded = padTo(nonce, 16);
            var tmp0 = new byte[][3];
            tmp0[0] = noncePadded;
            tmp0[1] = authTag;
            tmp0[2] = actualCipher;
            byte[] blob = concat(tmp0);
            String dnaSequence = bytesToDna(blob);
            String locus = bytesToHex(salt);
            return new ChimeraEngine.GenomePacket(dnaSequence, locus);
        } catch (Exception ex15) {
            Exception e = ex15;
            throw new ChimeraEngine.ChimeraException(("Chimera encode failed: " + e.getMessage()), e);
        }
    }

    public String decode(ChimeraEngine.GenomePacket packet) {
        try {
            byte[] salt = hexToBytes(packet.locus());
            byte[] blob = dnaToBytes(packet.sequence());
            if (blob.length < 32) throw new ChimeraEngine.ChimeraException("Chimera: blob qu\u00e1 ng\u1eafn.");
            byte[] noncePadded = Arrays.copyOf(blob, 16);
            byte[] authTag = Arrays.copyOfRange(blob, 16, 32);
            byte[] actualCipher = Arrays.copyOfRange(blob, 32, blob.length);
            byte[] nonce = Arrays.copyOf(noncePadded, 12);
            SecretKey key = deriveKey(salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(2, key, new GCMParameterSpec(128, nonce));
            var tmp0 = new byte[][2];
            tmp0[0] = actualCipher;
            tmp0[1] = authTag;
            byte[] ciphertextWithTag = concat(tmp0);
            byte[] plain = cipher.doFinal(ciphertextWithTag);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (ChimeraEngine.ChimeraException ex15) {
            ChimeraEngine.ChimeraException ce = ex15;
            throw ce;
        }
        } catch (Exception ex17) {
            Exception e = ex17;
            throw new ChimeraEngine.ChimeraException(("Chimera decode failed (invalid key or corrupted DNA): " + e.getMessage()), e);
        }
    }

    public String encodeToCompact(String plainText) {
        ChimeraEngine.GenomePacket p = encode(plainText);
        String seqB64 = Base64.getEncoder().encodeToString(p.sequence().getBytes(StandardCharsets.UTF_8));
        return (seqB64 + "." + p.locus());
    }

    public String decodeFromCompact(String compact) {
        int dot = compact.lastIndexOf(46);
        if (dot < 0) throw new ChimeraEngine.ChimeraException("Chimera: compact format invalid.");
        String seqB64 = compact.substring(0, dot);
        String locus = compact.substring((dot + 1));
        String sequence = new String(Base64.getDecoder().decode(seqB64), StandardCharsets.UTF_8);
        return decode(new ChimeraEngine.GenomePacket(sequence, locus));
    }

    private SecretKey deriveKey(byte[] salt) throws Exception {
        char[] passphrase = this.masterKey.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(passphrase, salt, 100000, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] rawKey = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(rawKey, "AES");
    }

    private static String bytesToDna(byte[] data) {
        StringBuilder sb = new StringBuilder((data.length * 4));
        Object var2 = data;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            byte b = var2[var4];
            int unsigned = (b & 255);
            int j = 0;
            while (j < 4) {
                sb.append(DNA_MAP[((unsigned >> (j * 2)) & 3)]);
                j++;
            }
            var4++;
        }
        return sb.toString();
    }

    private static byte[] dnaToBytes(String dna) {
        if ((dna.length() % 4) != 0) throw new ChimeraEngine.ChimeraException("Chimera: DNA sequence length must be multiple of 4.");
        byte[] result = new byte[(dna.length() / 4)];
        int i = 0;
        while (i < dna.length()) {
            int val = 0;
            int j = 0;
            while (j < 4) {
                char c = dna.charAt((i + j));
                if ((c >= DNA_REV_MAP.length) || (c != 65 && c != 84 && c != 71 && c != 67)) {
                    throw new ChimeraEngine.ChimeraException(("Chimera: Invalid DNA char '" + c + "' at index " + (i + j)));
                }
                val = (val | (DNA_REV_MAP[c] << (j * 2)));
                j++;
            }
            result[(i / 4)] = ((byte) val);
            i += 4;
        }
        return result;
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        byte[] result = arrays;
        int pos = result.length;
        int var4 = 0;
        while (var4 < pos) {
            byte[] a = result[var4];
            total = (total + a.length);
            var4++;
        }
        result = new byte[total];
        pos = 0;
        var4 = arrays;
        a = var4.length;
        int var6 = 0;
        while (var6 < a) {
            a = var4[var6];
            System.arraycopy(a, 0, result, pos, a.length);
            pos = (pos + a.length);
            var6++;
        }
        return result;
    }

    private static byte[] padTo(byte[] src, int targetLen) {
        if (src.length >= targetLen) return Arrays.copyOf(src, targetLen);
        byte[] result = new byte[targetLen];
        System.arraycopy(src, 0, result, 0, src.length);
        return result;
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
        if ((hex.length() % 2) != 0) throw new ChimeraEngine.ChimeraException("Chimera: hex length must be even.");
        byte[] result = new byte[(hex.length() / 2)];
        int i = 0;
        while (i < hex.length()) {
            result[(i / 2)] = ((byte) Integer.parseInt(hex.substring(i, (i + 2)), 16));
            i += 2;
        }
        return result;
    }

    static {
        var tmp0 = new char[4];
        tmp0[0] = 65;
        tmp0[1] = 84;
        tmp0[2] = 71;
        tmp0[3] = 67;
        DNA_MAP = tmp0;
        DNA_REV_MAP = new int[256];
        DNA_REV_MAP[65] = 0;
        DNA_REV_MAP[84] = 1;
        DNA_REV_MAP[71] = 2;
        DNA_REV_MAP[67] = 3;
        RNG = new SecureRandom();
    }


    public record GenomePacket(String sequence, String locus) {

        public GenomePacket(String sequence, String locus) {
            if ((sequence == null) || (sequence.isBlank())) {
                throw new ChimeraEngine.ChimeraException("Chimera: sequence must not be blank.");
            }
            if ((locus == null) || (locus.isBlank())) {
                throw new ChimeraEngine.ChimeraException("Chimera: locus must not be blank.");
            }
            this.sequence = sequence;
            this.locus = locus;
        }

        public String sequence() {
            return this.sequence;
        }

        public String locus() {
            return this.locus;
        }

    }

    public final class ChimeraException extends RuntimeException {
        public ChimeraException(String message) {
            super(message);
        }

        public ChimeraException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}
