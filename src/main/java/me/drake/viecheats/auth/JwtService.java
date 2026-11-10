package me.drake.viecheats.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class JwtService {
    private static final String HMAC_ALG = "HmacSHA256";
    private static final long MAX_TOKEN_AGE_SECONDS = 604800L;
    private final byte[] secretBytes;

    public JwtService(String secret) {
        if ((secret == null) || (secret.isBlank())) {
            throw new IllegalArgumentException("[JwtService] secret must not be blank.");
        }
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    public JwtService.Claims verify(String token, String expectedKey, String expectedHwid) {
        if (token == null) break label_1;
        if (!(token.isBlank())) break label_2;
        label_1:
        return null;
        label_2:
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;
        String signingInput = (parts[0] + "." + parts[1]);
        byte[] expectedSig = hmacSha256(signingInput.getBytes(StandardCharsets.UTF_8), this.secretBytes);
        try {
            byte[] actualSig = Base64.getUrlDecoder().decode(parts[2]);
        } catch (IllegalArgumentException ex8) {
            IllegalArgumentException e = ex8;
            return null;
        }
        if (!(Arrays.equals(expectedSig, actualSig))) {
            return null;
            try {
            }
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonObject payload = JsonParser.parseString(payloadJson).getAsJsonObject();
        } catch (Exception ex15) {
            e = ex15;
            return null;
        }
        long exp = getLong(payload, "exp", 0L);
        long iat = getLong(payload, "iat", 0L);
        if (exp <= 0L) return null;
        Instant now = Instant.now();
        Instant expiresAt = Instant.ofEpochSecond(exp);
        if (iat > 0L) {
        } else {
        }
        Instant issuedAt = null;
        if (now.isAfter(expiresAt.plusSeconds(30L))) return null;
        if (issuedAt != null && issuedAt.isAfter(now.plusSeconds(120L))) return null;
        if (issuedAt != null && now.isAfter(issuedAt.plusSeconds(604800L))) return null;
        String key = getString(payload, "key", getString(payload, "sub", ""));
        String hwid = getString(payload, "hwid", "");
        String plan = getString(payload, "plan", "");
        if (expectedKey != null && !(expectedKey.isBlank())) {
            if ((key.isBlank()) || (!(normalizeKey(key).equals(normalizeKey(expectedKey))))) {
                return null;
            }
        }
        if (expectedHwid != null && !(expectedHwid.isBlank())) {
            String normalizedHwid = expectedHwid.trim().toLowerCase(Locale.ROOT);
            if ((hwid.isBlank()) || (!(hwid.trim().toLowerCase(Locale.ROOT).equals(normalizedHwid)))) {
                return null;
            }
        }
        return new JwtService.Claims(key, hwid, plan, issuedAt, expiresAt);
    }

    public boolean isStructurallyValid(String token) {
        if (token == null) break label_1;
        if (token.isBlank()) {
            label_1:
            return false;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) return false;
        String signingInput = (parts[0] + "." + parts[1]);
        byte[] expectedSig = hmacSha256(signingInput.getBytes(StandardCharsets.UTF_8), this.secretBytes);
        try {
            byte[] actualSig = Base64.getUrlDecoder().decode(parts[2]);
        } catch (IllegalArgumentException ex8) {
            IllegalArgumentException e = ex8;
            return false;
        }
        return Arrays.equals(expectedSig, actualSig);
    }

    private static byte[] hmacSha256(byte[] data, byte[] key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (Exception ex4) {
            Exception e = ex4;
            throw new RuntimeException("[JwtService] HMAC-SHA256 failed", e);
        }
    }

    private static String getString(JsonObject obj, String key, String defaultValue) {
        if (obj != null && obj.has(key)) {
            if (obj.get(key).isJsonNull()) {
            }
            return defaultValue;
            try {
            }
            return obj.get(key).getAsString();
        } catch (Exception ex4) {
            Exception e = ex4;
            return defaultValue;
        }
    }

    private static long getLong(JsonObject obj, String key, long defaultValue) {
        if (obj != null && obj.has(key)) {
            if (obj.get(key).isJsonNull()) {
            }
            return defaultValue;
            try {
            }
            return obj.get(key).getAsLong();
        } catch (Exception ex4) {
            Exception e = ex4;
            return defaultValue;
        }
    }

    private static String normalizeKey(String key) {
        if (key == null) {
        } else {
        }
        return key.trim().toUpperCase(Locale.ROOT);
    }


    public final class Claims {
        private final String key;
        private final String hwid;
        private final String plan;
        private final Instant issuedAt;
        private final Instant expiresAt;

        public Claims(String key, String hwid, String plan, Instant issuedAt, Instant expiresAt) {
            this.key = (key == null ? "" : key);
            this.hwid = (hwid == null ? "" : hwid);
            this.plan = (plan == null ? "" : plan);
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
        }

        public String key() {
            return this.key;
        }

        public String hwid() {
            return this.hwid;
        }

        public String plan() {
            return this.plan;
        }

        public Instant issuedAt() {
            return this.issuedAt;
        }

        public Instant expiresAt() {
            return this.expiresAt;
        }

        public String toString() {
            return ("Claims{key='" + this.key + "', hwid='" + this.hwid + "', plan='" + this.plan + "', iat=" + String.valueOf(this.issuedAt) + ", exp=" + String.valueOf(this.expiresAt) + "}");
        }

    }
}
