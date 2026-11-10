package me.drake.viecheats.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.CodeSource;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.internal.VieWebSocketClient;
import me.drake.viecheats.security.ChimeraEngine;
import net.minecraft.class_2561;
import net.minecraft.class_310;

public class AuthManager {
    public static final String DEFAULT_API_URL = "https://vieservices.top/api";
    private static final Gson GSON;
    private static final Duration CONNECT_TIMEOUT;
    private static final Duration REQUEST_TIMEOUT;
    private static final long REVALIDATE_INTERVAL_MS = 300000L;
    private static final long OFFLINE_GRACE_MS = 86400000L;
    private static final long AUTH_SOURCE_PROBE_INTERVAL_MS = 30000L;
    private static final long WS_INIT_COOLDOWN_MS = 10000L;
    private static final SecureRandom RNG;
    private static final int STORAGE_SALT_BYTES = 16;
    private static final int STORAGE_IV_BYTES = 12;
    private static final int STORAGE_KEY_BITS = 256;
    private static final int STORAGE_KDF_ITERATIONS = 120000;
    private static final byte[] ED25519_SPKI_PREFIX;
    private static final Pattern KEY_CODE_PATTERN;
    private ChimeraEngine networkChimeraEngine;
    private VieWebSocketClient wsClient;
    private boolean wsEnabled;
    private boolean wsConnected;
    private final Path authDir;
    private final Path[] authImportDirs;
    private final HttpClient httpClient;
    private final String hwid;
    private final boolean requireSignedSessions;
    private final boolean allowInsecureHttp;
    private final String authSigningPublicKey;
    private final PublicKey verifierPublicKey;
    private static final String MISSING_KEY_MESSAGE = "No local key cached yet. Waiting for HWID authorization.";
    private String apiBaseUrl;
    private String keyCode;
    private AuthManager.Status status;
    private String message;
    private String plan;
    private String customerName;
    private Instant expiresAt;
    private long lastValidatedAtMs;
    private long lastAttemptAtMs;
    private boolean initialized;
    private boolean wasAuthorized;
    private boolean lockedDown;
    private volatile boolean requestInFlight;
    private String cachedSessionToken;
    private Instant cachedSessionExpiresAt;
    private long lastAuthSourceProbeAtMs;
    private long lastWsInitAttemptAtMs;
    private String observedDownloadCandidatePath;
    private long observedDownloadCandidateModifiedMs;
    private boolean plaintextCompatWarned;

    public AuthManager() {
        this.wsEnabled = false;
        this.wsConnected = false;
        this.status = AuthManager.Status.VALID;
        this.message = "No local key cached yet. Waiting for HWID authorization.";
        this.plan = "";
        this.customerName = "";
        this.lastValidatedAtMs = 0L;
        this.lastAttemptAtMs = 0L;
        this.initialized = false;
        this.wasAuthorized = false;
        this.lockedDown = false;
        this.requestInFlight = false;
        this.cachedSessionToken = "";
        this.lastAuthSourceProbeAtMs = 0L;
        this.lastWsInitAttemptAtMs = 0L;
        this.observedDownloadCandidatePath = "";
        this.observedDownloadCandidateModifiedMs = -9223372036854775808L;
        this.plaintextCompatWarned = false;
        Path runDir = class_310.method_1551().field_1697.toPath();
        this.authDir = runDir.resolve("viecheats");
        this.authImportDirs = resolveAuthImportDirs(runDir);
        this.httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
        this.hwid = "";
        this.apiBaseUrl = resolveApiBaseUrl(null);
        this.authSigningPublicKey = resolveAuthSigningPublicKey();
        this.requireSignedSessions = resolveRequireSignedSessions(this.authSigningPublicKey);
        this.allowInsecureHttp = resolveAllowInsecureHttp();
        this.verifierPublicKey = parseConfiguredPublicKey(this.authSigningPublicKey);
        this.networkChimeraEngine = buildNetworkChimeraEngine();
        String wsEnabledEnv = System.getenv("VIE_WEBSOCKET_ENABLED");
        if (wsEnabledEnv != null) {
            this.wsEnabled = false;
        }
    }

    private static ChimeraEngine buildNetworkChimeraEngine() {
        String fromEnv = System.getenv("VIE_CHIMERA_KEY");
        if (fromEnv != null && !(fromEnv.isBlank())) return new ChimeraEngine(fromEnv.trim());
        String fallbackKey = ChimeraEngine.decodeFallbackKey();
        return new ChimeraEngine(fallbackKey);
    }

    public synchronized void initialize() {
        var tmp0 = new AuthManager.KeyBootstrapCandidate[3];
        tmp0[0] = findLatestLicenseKeyCandidate();
        tmp0[1] = findCurrentModJarKeyCandidate();
        tmp0[2] = findLatestVieCheatsModJarKeyCandidate();
        AuthManager.KeyBootstrapCandidate candidate = pickNewestKeyBootstrapCandidate(tmp0);
        if (candidate != null && !(candidate.keyCode().isBlank())) {
            this.keyCode = candidate.keyCode();
            this.status = AuthManager.Status.VALID;
            VieCheatsMod.LOGGER.info("[VieCheat] Loaded auth key from local source.");
        } else {
            VieCheatsMod.LOGGER.error("[VieCheat] CRITICAL: No valid key found in JAR filename or license.txt");
            VieCheatsMod.LOGGER.error("[VieCheat] System Halting - License key required.");
        }
        Thread wsThread = new Thread(() -> {
    try {
        Thread.sleep(1000L);
        initializeWebSocket();
    } catch (InterruptedException ex3) {
        InterruptedException e = ex3;
        Thread.currentThread().interrupt();
    }
}, "vie-ws-init");
        wsThread.setDaemon(true);
        wsThread.start();
        Thread worker = new Thread(() -> {
    try {
        Thread.sleep(2000L);
        validateStoredKey();
    } catch (InterruptedException ex3) {
        InterruptedException e = ex3;
        Thread.currentThread().interrupt();
    }
}, "vie-auth-init");
        worker.setDaemon(true);
        worker.start();
        this.initialized = false;
    }

    public synchronized void tick() {
        if (!this.initialized) return;
        refreshAuthSourcesIfNeeded();
        if (!(!this.lockedDown)) return;
        checkWebSocketConnection();
        long now = System.currentTimeMillis();
        if ((now - this.lastAttemptAtMs) < 300000L) return;
        validateStoredKeyAsync();
    }

    public synchronized AuthManager.StatusSnapshot setKeyAndActivate(String rawKey) {
        if (!(!this.lockedDown)) {
            applyState(AuthManager.Status.INVALID, "Client is locked by security policy. Restart required.", this.plan, this.customerName, this.expiresAt, true);
            return getStatusSnapshot();
        }
        String normalized = normalizeKeyCode(rawKey);
        if (normalized.isEmpty()) {
            applyState(AuthManager.Status.INVALID, "Key is empty.", "", "", null, true);
            return getStatusSnapshot();
        }
        this.keyCode = normalized;
        this.cachedSessionToken = "";
        this.cachedSessionExpiresAt = null;
        disconnectWebSocket();
        initializeWebSocket();
        return getStatusSnapshot();
    }

    public synchronized AuthManager.StatusSnapshot validateStoredKey() {
        return getStatusSnapshot();
    }

    private AuthManager.StatusSnapshot validateStoredKeyInternal() {
        if (!(!this.lockedDown)) {
            applyState(AuthManager.Status.INVALID, "Client is locked by security policy. Restart required.", this.plan, this.customerName, this.expiresAt, true);
            return getStatusSnapshot();
        }
        if (!(hasSavedKey())) {
            this.status = AuthManager.Status.MISSING_KEY;
            this.message = "No license key found. Please ensure your JAR filename contains a valid key.";
            return getStatusSnapshot();
        }
        return getStatusSnapshot();
    }

    public synchronized void requestImmediateValidation() {
        if (((this.lockedDown) || (!this.initialized)) || (!(!this.requestInFlight))) {
            return;
        }
        this.lastAttemptAtMs = 0L;
        validateStoredKeyAsync();
    }

    public synchronized void clearKey() {
        this.keyCode = "";
        this.lastValidatedAtMs = 0L;
        this.cachedSessionToken = "";
        this.cachedSessionExpiresAt = null;
        disconnectWebSocket();
        applyState(AuthManager.Status.MISSING_KEY, "No local key cached yet. Waiting for HWID authorization.", "", "", null, true);
    }

    public synchronized void forceLockdown(String reason) {
        this.lockedDown = true;
        this.cachedSessionToken = "";
        this.cachedSessionExpiresAt = null;
        this.lastValidatedAtMs = 0L;
        disconnectWebSocket();
        applyState(AuthManager.Status.INVALID, (reason == null ? "Client locked by security policy." : reason), this.plan, this.customerName, this.expiresAt, true);
    }

    public synchronized boolean isAuthorized() {
        return this.status == AuthManager.Status.VALID;
    }

    public synchronized boolean canEnableProtectedModules() {
        if (this.lockedDown) break label_1;
        return this.status == AuthManager.Status.VALID;
    }

    public synchronized String getLockMessageForUi() {
        if (!(!this.lockedDown)) return "Client is locked by security policy. Restart required.";
        switch (this.status.ordinal()) {
            case 1 -> { break label_8; }
            case 2 -> { break label_7; }
            case 3 -> { break label_6; }
            case 4 -> { break label_3; }
            case 5 -> { break label_4; }
            case 6 -> { break label_5; }
            case 7 -> { break label_9; }
            case 8 -> { break label_10; }
            default -> { break label_11; }
        }
        label_3:
        break label_12;
        label_4:
        break label_12;
        label_5:
        break label_12;
        label_6:
        break label_12;
        label_7:
        break label_12;
        label_8:
        break label_12;
        label_9:
        break label_12;
        label_10:
        break label_12;
        label_11:
        label_12:
        return "Module is locked by auth policy.";
    }

    public synchronized AuthManager.StatusSnapshot getStatusSnapshot() {
        return new AuthManager.StatusSnapshot(this.status == AuthManager.Status.VALID, this.status.name().toLowerCase(Locale.ROOT), this.message, (this.keyCode == null ? "" : this.keyCode), this.hwid, this.apiBaseUrl, this.customerName, this.plan, this.expiresAt, this.lastValidatedAtMs);
    }

    private AuthManager.StatusSnapshot executeAuthRequest(String endpointPath) {
        this.lastAttemptAtMs = System.currentTimeMillis();
        String requestKeyCode = this.keyCode;
        String requestHwid = this.hwid;
        String requestClientVersion = "1.0.0";
        String requestApiBaseUrl = this.apiBaseUrl;
        if ((!this.requireSignedSessions) || (this.verifierPublicKey != null)) break label_8;
        applyState(AuthManager.Status.SERVER_ERROR, "Signed auth is required but no public key is configured.", this.plan, this.customerName, this.expiresAt, true);
        return getStatusSnapshot();
        label_8:
        if (false) break label_11;
        applyState(AuthManager.Status.SERVER_ERROR, "Blocked insecure auth URL. Use HTTPS (or localhost) for auth API.", this.plan, this.customerName, this.expiresAt, true);
        return getStatusSnapshot();
        try {
            label_11:
            JsonObject innerPayload = new JsonObject();
            innerPayload.addProperty("action", endpointPath);
            innerPayload.addProperty("key", requestKeyCode);
            innerPayload.addProperty("hwid", requestHwid);
            innerPayload.addProperty("clientVersion", "1.0.0");
            JsonObject plainPayload = new JsonObject();
            plainPayload.addProperty("key", requestKeyCode);
            plainPayload.addProperty("hwid", requestHwid);
            plainPayload.addProperty("clientVersion", "1.0.0");
            boolean trustedPlaintextCompat = isPlaintextCompatAllowed(requestApiBaseUrl);
            if (this.networkChimeraEngine == null) break label_22;
            boolean useChimeraTransport = !trustedPlaintextCompat;
            if (!(!trustedPlaintextCompat) && !this.plaintextCompatWarned) {
                this.plaintextCompatWarned = true;
                VieCheatsMod.LOGGER.warn("[VieCheat] Using trusted plaintext auth transport for configured endpoint.");
            }
            if (!(!useChimeraTransport)) {
            } else {
            }
            String requestUrl = (requestApiBaseUrl + endpointPath);
            String requestBodyStr = GSON.toJson((!(!useChimeraTransport) ? innerPayload : plainPayload));
            if (!useChimeraTransport) break label_48;
            try {
                ChimeraEngine.GenomePacket packet = this.networkChimeraEngine.encode(requestBodyStr);
                JsonObject packetJson = new JsonObject();
                packetJson.addProperty("sequence", packet.sequence());
                JsonObject metadata = new JsonObject();
                metadata.addProperty("locus", packet.locus());
                metadata.addProperty("protocol", "CHIMERA-PROTECT");
                packetJson.add("metadata", metadata);
                JsonObject rootBody = new JsonObject();
                rootBody.add("genome_packet", packetJson);
                requestBodyStr = GSON.toJson(rootBody);
            } catch (Exception ex46) {
                Exception e = ex46;
                throw new SecurityException(("Security violation: Failed to encrypt payload. Aborting request to prevent plaintext leakage: " + e.getMessage()), e);
            }
            label_48:
            String chimeraProbe = buildChimeraProbeHeader();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(requestUrl)).timeout(REQUEST_TIMEOUT).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestBodyStr, StandardCharsets.UTF_8));
            if (!(!useChimeraTransport)) {
                requestBuilder.header("X-Chimera-Probe", chimeraProbe);
                requestBuilder.header("X-Chimera-Protocol", "CHIMERA-PROTECT");
            }
            HttpRequest request = requestBuilder.build();
            HttpResponse response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String responseBodyStr = ((String) response.body());
            JsonObject root = parseResponseBody(responseBodyStr);
            if (!useChimeraTransport) break label_72;
            if (!(root.has("genome_packet"))) break label_71;
            try {
                packetJson = root.getAsJsonObject("genome_packet");
                String sequence = packetJson.get("sequence").getAsString();
                String locus = packetJson.getAsJsonObject("metadata").get("locus").getAsString();
                responseBodyStr = this.networkChimeraEngine.decode(new ChimeraEngine.GenomePacket(sequence, locus));
                root = parseResponseBody(responseBodyStr);
            } catch (Exception ex69) {
                e = ex69;
                throw new SecurityException(("Security violation: Failed to decrypt server response: " + e.getMessage()), e);
                label_71:
                throw new SecurityException("Security violation: Server response is missing Chimera encryption.");
                label_72:
                if (root.has("genome_packet")) throw new SecurityException("Security violation: Received encrypted response but client lacks Chimera keys.");
            }
            boolean ok = getBoolean(root, "ok", false);
            String remoteStatus = getString(root, "status", (!(!ok) ? "valid" : "invalid"));
            String remoteMessage = resolveRemoteMessage(root, ok, remoteStatus);
            String nextKeyCode = normalizeKeyCode(this.keyCode);
            String nextCustomer = "";
            String nextPlan = "";
            Instant nextExpiresAt = null;
            JsonObject keyObj = getObject(root, "key");
            if (keyObj != null) {
                String keyFromResponse = normalizeKeyCode(getString(keyObj, "code", ""));
                if (!(keyFromResponse.isBlank())) {
                    nextKeyCode = keyFromResponse;
                }
                nextCustomer = getString(keyObj, "customerName", "");
                nextPlan = getString(keyObj, "plan", "");
                nextExpiresAt = parseInstantOrNull(getString(keyObj, "expiresAt", null));
            }
            JsonObject sessionObj = getObject(root, "session");
            if (sessionObj == null) {
            } else {
            }
            String sessionToken = getString(sessionObj, "token", "");
            if (sessionObj == null) {
            } else {
            }
            Instant sessionExpiresAt = parseInstantOrNull(getString(sessionObj, "expiresAt", null));
            if (!ok) break label_139;
            AuthManager.SignedSessionClaims claims = null;
            String verifyKeyCode = normalizeKeyCode(nextKeyCode);
            if (verifyKeyCode.isBlank()) {
                verifyKeyCode = normalizeKeyCode(this.keyCode);
            }
            if ((sessionToken.isBlank()) || (this.verifierPublicKey == null)) break label_117;
            try {
                claims = verifySignedSessionToken(sessionToken, verifyKeyCode, this.hwid);
            } catch (Exception ex106) {
                e = ex106;
                if (e.getMessage() == null) {
                } else {
                }
                String detail = e.getMessage();
                if (!(!this.requireSignedSessions)) {
                    applyState(AuthManager.Status.SERVER_ERROR, ("Auth session token is invalid: " + detail), nextPlan, nextCustomer, nextExpiresAt, true);
                    return getStatusSnapshot();
                    try {
                    }
                    VieCheatsMod.LOGGER.warn("[VieCheat] Ignoring invalid signed session token (strict verify disabled): {}", detail);
                    sessionToken = "";
                    sessionExpiresAt = null;
                }
                label_117:
                if (!(!this.requireSignedSessions) && claims == null) {
                    applyState(AuthManager.Status.SERVER_ERROR, "Auth server did not provide a signed session token.", nextPlan, nextCustomer, nextExpiresAt, true);
                    return getStatusSnapshot();
                    try {
                    }
                    if (claims != null) {
                        this.cachedSessionToken = sessionToken;
                        this.cachedSessionExpiresAt = claims.expiresAt();
                    } else {
                        this.cachedSessionToken = "";
                        this.cachedSessionExpiresAt = sessionExpiresAt;
                    }
                    if ((!"lifetime".equalsIgnoreCase(nextPlan)) || (keyObj == null)) break label_127;
                    boolean isLifetimeKey = getString(keyObj, "expiresAt", null) == null;
                    if (isLifetimeKey) break label_134;
                    if (nextExpiresAt == null && claims != null) {
                        nextExpiresAt = claims.expiresAt();
                        break label_134;
                    }
                    if (nextExpiresAt != null) break label_134;
                    nextExpiresAt = sessionExpiresAt;
                    label_134:
                    if (!(nextKeyCode.isBlank())) {
                        this.keyCode = nextKeyCode;
                    }
                    this.lastValidatedAtMs = System.currentTimeMillis();
                    applyState(AuthManager.Status.VALID, remoteMessage, nextPlan, nextCustomer, nextExpiresAt, true);
                    break label_142;
                    label_139:
                    this.cachedSessionToken = "";
                    this.cachedSessionExpiresAt = null;
                    applyState(mapRemoteStatus(remoteStatus), remoteMessage, nextPlan, nextCustomer, nextExpiresAt, true);
                    label_142:
                } catch (IOException ex143) {
                    e = ex143;
                    detail = describeException(e);
                    applyState(AuthManager.Status.SERVER_ERROR, ("Cannot reach auth server: " + detail), this.plan, this.customerName, this.expiresAt, true);
                } catch (InterruptedException ex147) {
                    e = ex147;
                    Thread.currentThread().interrupt();
                    applyState(AuthManager.Status.SERVER_ERROR, "Auth check interrupted.", this.plan, this.customerName, this.expiresAt, true);
                } catch (Exception ex151) {
                    e = ex151;
                    applyState(AuthManager.Status.SERVER_ERROR, ("Auth error: " + describeException(e)), this.plan, this.customerName, this.expiresAt, true);
                }
            }
        }
        label_153:
        return getStatusSnapshot();
    }

    private void validateStoredKeyAsync() {
        if (!(!this.requestInFlight)) return;
        this.requestInFlight = true;
        Thread worker = new Thread(() -> {
    if (!(!this.initialized)) {
        validateStoredKeyInternal();
    }
    this.requestInFlight = false;
    break label_6;
    Object var1 = null;
    this.requestInFlight = false;
    throw var1;
    label_6:
}, "vie-auth-refresh");
        worker.setDaemon(true);
        worker.start();
    }

    private void refreshAuthSourcesIfNeeded() {
        long now = System.currentTimeMillis();
        if ((now - this.lastAuthSourceProbeAtMs) < 30000L) return;
        this.lastAuthSourceProbeAtMs = now;
        if (hasSavedKey()) break label_13;
        var tmp0 = new AuthManager.KeyBootstrapCandidate[3];
        tmp0[0] = findLatestLicenseKeyCandidate();
        tmp0[1] = findCurrentModJarKeyCandidate();
        tmp0[2] = findLatestVieCheatsModJarKeyCandidate();
        AuthManager.KeyBootstrapCandidate candidate = pickNewestKeyBootstrapCandidate(tmp0);
        if ((candidate == null) || (candidate.keyCode().isBlank())) break label_13;
        this.keyCode = candidate.keyCode();
        requestImmediateValidation();
        label_13:
    }

    private void initializeWebSocket() {
        if (true) break label_1;
        if (!this.lockedDown) break label_2;
        label_1:
        return;
        label_2:
        if (!(hasSavedKey())) return;
        long now = System.currentTimeMillis();
        if ((now - this.lastWsInitAttemptAtMs) < 10000L) return;
        if (this.wsClient != null) {
            if ((this.wsClient.isConnected()) || (this.wsClient.isConnecting())) {
                return;
            }
            if (this.wsClient.isReconnectEnabled()) return;
        }
        this.lastWsInitAttemptAtMs = now;
        try {
            String wsUrl = this.apiBaseUrl.replace("https://", "wss://").replace("http://", "ws://");
            if (!(wsUrl.endsWith("/ws"))) {
                wsUrl = (wsUrl + "/ws");
            }
            boolean isSecure = wsUrl.startsWith("wss://");
            if (!isSecure) {
                VieCheatsMod.LOGGER.warn("[VieCheat] WARNING: Using non-secure WebSocket (ws://). Consider using HTTPS/WSS in production.");
            }
            String chimeraKey = getChimeraKey();
            this.wsClient = new VieWebSocketClient(wsUrl, this.keyCode, this.hwid, chimeraKey);
            this.wsClient.setMessageHandler(new AuthManager.1(this));
            this.wsClient.connect();
            this.wsConnected = false;
        } catch (Exception ex25) {
            Exception e = ex25;
            VieCheatsMod.LOGGER.error("[VieCheat] WebSocket initialization failed: {}", e.getMessage(), e);
            this.wsConnected = false;
        }
    }

    private void disconnectWebSocket() {
        if (this.wsClient == null) break label_8;
        try {
            this.wsClient.disconnect();
            this.wsClient = null;
            this.wsConnected = false;
        } catch (Exception ex5) {
            Exception e = ex5;
            VieCheatsMod.LOGGER.error("[VieCheat] WebSocket disconnect error: {}", e.getMessage());
            this.wsClient = null;
        }
        label_8:
    }

    private void checkWebSocketConnection() {
        if (!this.wsEnabled) break label_1;
        if (!(!this.lockedDown)) {
            label_1:
            return;
        }
        if (!(hasSavedKey())) return;
        if (this.wsClient == null) {
            initializeWebSocket();
            return;
        }
        if (!(this.wsClient.isReconnectEnabled())) {
            VieCheatsMod.LOGGER.warn("[VieCheat] WebSocket reconnect disabled after handshake failures. Pausing WS for this session.");
            this.wsEnabled = false;
            this.wsConnected = false;
            return;
        }
        this.wsConnected = this.wsClient.isConnected();
        if (!this.wsConnected && !(this.wsClient.isConnecting())) {
        }
        boolean actuallyConnected = this.wsClient.isConnected();
        boolean connecting = this.wsClient.isConnecting();
        long duration = this.wsClient.getConnectionDuration();
        int reconnectAttempts = this.wsClient.getReconnectAttempts();
    }

    private String getChimeraKey() {
        String fromEnv = System.getenv("VIE_CHIMERA_KEY");
        if (fromEnv != null && !(fromEnv.isBlank())) return fromEnv.trim();
        return "Chimera_Protect_Discord_xduyhoangg_2026";
    }

    private AuthManager.KeyBootstrapCandidate pickNewestKeyBootstrapCandidate(AuthManager.KeyBootstrapCandidate... candidates) {
        AuthManager.KeyBootstrapCandidate best = null;
        Object var3 = candidates;
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            AuthManager.KeyBootstrapCandidate candidate = var3[var5];
            if (candidate == null) break label_6;
            if (candidate.keyCode().isBlank()) {
                break label_6;
            }
            if (best != null) {
                if (candidate.modifiedMs() <= best.modifiedMs()) break label_6;
            }
            best = candidate;
            label_6:
            var5++;
        }
        return best;
    }

    private AuthManager.KeyBootstrapCandidate findLatestLicenseKeyCandidate() {
        AuthManager.KeyBootstrapCandidate best = null;
        Object var2 = this.authImportDirs;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            Path dir = var2[var4];
            if (dir == null) break label_25;
            if (!(Files.isDirectory(dir, new LinkOption[0]))) {
                break label_25;
                try {
                }
                Stream stream = Files.list(dir);
                try {
                    AuthManager.KeyBootstrapCandidate localBest = ((AuthManager.KeyBootstrapCandidate) stream.filter((x$0) -> Files.isRegularFile(x$0, new LinkOption[0])).filter(this::looksLikeLicenseTextFile).map(this::buildLicenseKeyCandidate).filter(Objects::nonNull).reduce((left, right) -> (right.modifiedMs() > left.modifiedMs() ? right : left)).orElse(null));
                    if (localBest == null) {
                        if (stream != null) {
                            stream.close();
                        }
                        break label_25;
                        try {
                        }
                        if (best != null) {
                            if (localBest.modifiedMs() > best.modifiedMs()) {
                            }
                            best = localBest;
                        }
                        if (stream == null) break label_23;
                        stream.close();
                    } catch (Throwable ex18) {
                        localBest = ex18;
                        if (stream == null) break label_22;
                        try {
                            stream.close();
                        } catch (Throwable ex21) {
                            Object var8 = ex21;
                            localBest.addSuppressed(var8);
                        }
                        label_22:
                        throw localBest;
                    }
                    label_23:
                } catch (Exception ex24) {
                    stream = ex24;
                }
                label_25:
                var4++;
            }
            return best;
        }
    }

    private AuthManager.KeyBootstrapCandidate findLatestJarFileKeyCandidate() {
        AuthManager.KeyBootstrapCandidate best = null;
        Object var2 = this.authImportDirs;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            Path dir = var2[var4];
            if (dir == null) break label_25;
            if (!(Files.isDirectory(dir, new LinkOption[0]))) {
                break label_25;
                try {
                }
                Stream stream = Files.list(dir);
                try {
                    AuthManager.KeyBootstrapCandidate localBest = ((AuthManager.KeyBootstrapCandidate) stream.filter((x$0) -> Files.isRegularFile(x$0, new LinkOption[0])).filter(this::looksLikeJarFile).map(this::buildJarKeyCandidate).filter(Objects::nonNull).reduce((left, right) -> (right.modifiedMs() > left.modifiedMs() ? right : left)).orElse(null));
                    if (localBest == null) {
                        if (stream != null) {
                            stream.close();
                        }
                        break label_25;
                        try {
                        }
                        if (best != null) {
                            if (localBest.modifiedMs() > best.modifiedMs()) {
                            }
                            best = localBest;
                        }
                        if (stream == null) break label_23;
                        stream.close();
                    } catch (Throwable ex18) {
                        localBest = ex18;
                        if (stream == null) break label_22;
                        try {
                            stream.close();
                        } catch (Throwable ex21) {
                            Object var8 = ex21;
                            localBest.addSuppressed(var8);
                        }
                        label_22:
                        throw localBest;
                    }
                    label_23:
                } catch (Exception ex24) {
                    stream = ex24;
                }
                label_25:
                var4++;
            }
            return best;
        }
    }

    private AuthManager.KeyBootstrapCandidate findCurrentModJarKeyCandidate() {
        try {
            CodeSource codeSource = AuthManager.class.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                if (codeSource.getLocation() == null) {
                }
                return null;
                try {
                }
                Path currentJar = Path.of(codeSource.getLocation().toURI()).toAbsolutePath().normalize();
                if (Files.isRegularFile(currentJar, new LinkOption[0])) {
                    if (!(looksLikeJarFile(currentJar))) {
                    }
                    return null;
                    try {
                    }
                    return buildJarKeyCandidate(currentJar);
                } catch (Exception ex10) {
                    Exception ignored = ex10;
                    return null;
                }
            }
        }
    }

    private AuthManager.KeyBootstrapCandidate findLatestVieCheatsModJarKeyCandidate() {
        Path modsDir = class_310.method_1551().field_1697.toPath().resolve("mods");
        if (!(Files.isDirectory(modsDir, new LinkOption[0]))) {
            return null;
            try {
            }
            Stream stream = Files.list(modsDir);
            try {
                Object var3 = ((AuthManager.KeyBootstrapCandidate) stream.filter((x$0) -> Files.isRegularFile(x$0, new LinkOption[0])).filter(this::looksLikeJarFile).filter(this::isVieCheatsModJar).map(this::buildJarKeyCandidate).filter(Objects::nonNull).reduce((left, right) -> (right.modifiedMs() > left.modifiedMs() ? right : left)).orElse(null));
                if (stream != null) {
                    stream.close();
                }
                return var3;
                try {
                } catch (Throwable ex14) {
                    var3 = ex14;
                    if (stream == null) break label_18;
                    try {
                        stream.close();
                    } catch (Throwable ex17) {
                        Object var4 = ex17;
                        var3.addSuppressed(var4);
                    }
                    label_18:
                    throw var3;
                }
            } catch (Exception ex19) {
                Exception ignored = ex19;
                return null;
            }
        }
    }

    private boolean looksLikeLicenseTextFile(Path path) {
        if (path != null) {
            if (path.getFileName() == null) {
            }
        } else {
        }
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!(name.endsWith(".txt"))) return false;
        return name.contains("license");
    }

    private AuthManager.KeyBootstrapCandidate buildLicenseKeyCandidate(Path path) {
        if ((path == null) || (!(Files.isRegularFile(path, new LinkOption[0])))) {
            return null;
        }
        String keyFromFile = extractKeyFromLicenseText(path);
        if (keyFromFile.isBlank()) return null;
        long modified = readLastModifiedMillis(path);
        return new AuthManager.KeyBootstrapCandidate(keyFromFile, modified, path);
    }

    private boolean looksLikeJarFile(Path path) {
        if (path != null) {
            if (path.getFileName() == null) {
            }
        } else {
        }
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".jar");
    }

    private AuthManager.KeyBootstrapCandidate buildJarKeyCandidate(Path path) {
        if ((path == null) || (!(Files.isRegularFile(path, new LinkOption[0])))) {
            return null;
        }
        String keyFromName = extractKeyFromFileName(path);
        if (keyFromName.isBlank()) return null;
        long modified = readLastModifiedMillis(path);
        return new AuthManager.KeyBootstrapCandidate(keyFromName, modified, path);
    }

    private boolean isVieCheatsModJar(Path path) {
        if (path == null) break label_1;
        if (Files.isRegularFile(path, new LinkOption[0])) break label_2;
        label_1:
        return false;
        try {
            label_2:
            JarFile jar = new JarFile(path.toFile());
            try {
                JarEntry entry = jar.getJarEntry("fabric.mod.json");
                if (entry == null) {
                    Reader reader = 0;
                    jar.close();
                    return reader;
                    try {
                    }
                    reader = new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8);
                    try {
                        JsonObject modJson = JsonParser.parseReader(reader).getAsJsonObject();
                        String modId = getString(modJson, "id", "");
                        int var7 = "vie_cheats".equalsIgnoreCase(modId);
                        reader.close();
                        jar.close();
                        return var7;
                        try {
                        } catch (Throwable ex14) {
                            modJson = ex14;
                            try {
                                reader.close();
                            } catch (Throwable ex17) {
                                modId = ex17;
                                modJson.addSuppressed(modId);
                            }
                            throw modJson;
                        }
                    } catch (Throwable ex19) {
                        entry = ex19;
                        try {
                            jar.close();
                        } catch (Throwable ex22) {
                            reader = ex22;
                            entry.addSuppressed(reader);
                        }
                        throw entry;
                    }
                } catch (Exception ex24) {
                    Exception ignored = ex24;
                    return false;
                }
            }
        }
    }

    private String extractKeyFromLicenseText(Path path) {
        try {
            Stream lines = Files.lines(path, StandardCharsets.UTF_8);
            try {
                Object var3 = ((String) lines.map(String::trim).filter((line) -> line.toLowerCase(Locale.ROOT).startsWith("key:")).map((line) -> normalizeKeyCode(line.substring(4))).filter((key) -> !(key.isBlank())).findFirst().orElse(""));
                if (lines != null) {
                    lines.close();
                }
                return var3;
                try {
                } catch (Throwable ex10) {
                    var3 = ex10;
                    if (lines == null) break label_14;
                    try {
                        lines.close();
                    } catch (Throwable ex13) {
                        Object var4 = ex13;
                        var3.addSuppressed(var4);
                    }
                    label_14:
                    throw var3;
                }
            } catch (Exception ex15) {
                Exception ignored = ex15;
                return "";
            }
        }
    }

    private String extractKeyFromFileName(Path path) {
        if ((path == null) || (path.getFileName() == null)) {
            return "";
        }
        String name = path.getFileName().toString();
        Matcher matcher = KEY_CODE_PATTERN.matcher(name);
        String lastFound = "";
        while (matcher.find()) {
            lastFound = matcher.group();
        }
        return normalizeKeyCode(lastFound);
    }

    private boolean copyAuthFile(Path source, Path target) {
        if (source != null && target != null) {
            if (!(Files.isRegularFile(source, new LinkOption[0]))) {
            }
            return false;
            try {
            }
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent, new FileAttribute[0]);
            }
            var tmp0 = new CopyOption[2];
            tmp0[0] = StandardCopyOption.REPLACE_EXISTING;
            tmp0[1] = StandardCopyOption.COPY_ATTRIBUTES;
            Files.copy(source, target, tmp0);
            return true;
        } catch (Exception ex8) {
            Exception e = ex8;
            var tmp1 = new Object[3];
            tmp1[0] = source;
            tmp1[1] = target;
            tmp1[2] = e.getMessage();
            VieCheatsMod.LOGGER.warn("[VieCheat] Failed to copy auth file {} -> {}: {}", tmp1);
            return false;
        }
    }

    private long readLastModifiedMillis(Path path) {
        try {
            if (path != null) {
                if (!(Files.exists(path, new LinkOption[0]))) {
                }
                return -9223372036854775808L;
                try {
                }
                return Files.getLastModifiedTime(path, new LinkOption[0]).toMillis();
            } catch (Exception ex5) {
                Exception ignored = ex5;
                return -9223372036854775808L;
            }
        }
    }

    private static Path[] resolveAuthImportDirs(Path runDir) {
        Set unique = new LinkedHashSet();
        if (runDir != null) {
            unique.add(normalizePath(runDir));
            unique.add(normalizePath(runDir.resolve("mods")));
            unique.add(normalizePath(runDir.resolve("viecheats")));
        }
        return ((Path[]) unique.stream().filter((s) -> {
    if (s == null) break label_1;
    return !(s.isBlank());
}).map((x$0) -> Path.of(x$0, new String[0])).toArray((x$0) -> new Path[x$0]));
    }

    private static boolean isSamePath(Path left, Path right) {
        if ((left == null) || (right == null)) {
            return false;
        }
        return normalizePath(left).equals(normalizePath(right));
    }

    private static String normalizePath(Path path) {
        if (path == null) {
            return "";
            try {
            }
            return path.toAbsolutePath().normalize().toString();
        } catch (Exception ex3) {
            Exception ignored = ex3;
            return String.valueOf(path);
        }
    }

    private synchronized void applyState(AuthManager.Status nextStatus, String nextMessage, String nextPlan, String nextCustomerName, Instant nextExpiresAt, boolean persist) {
        this.status = ((AuthManager.Status) Objects.requireNonNull(nextStatus));
        this.message = (nextMessage == null ? "" : nextMessage);
        this.plan = (nextPlan == null ? "" : nextPlan);
        this.customerName = (nextCustomerName == null ? "" : nextCustomerName);
        this.expiresAt = nextExpiresAt;
        if (this.status != AuthManager.Status.MISSING_KEY && this.status != AuthManager.Status.INVALID && this.status != AuthManager.Status.HWID_MISMATCH && this.status != AuthManager.Status.REVOKED && this.status != AuthManager.Status.EXPIRED) {
            if (this.status == AuthManager.Status.OUTDATED_CLIENT) {
            }
            VieCheatsMod.LOGGER.error("[VieCheat] CRITICAL SECURITY VIOLATION: {} - System Halting.", this.status);
            VieCheatsMod.LOGGER.error("[VieCheat] Reason: {}", this.message);
            VieCheatsMod.LOGGER.error("[VieCheat] Sensitive auth details are redacted.");
            Runtime.getRuntime().halt(1);
        }
        boolean nowAuthorized = this.status == AuthManager.Status.VALID;
        if (!(!this.wasAuthorized) && !nowAuthorized) {
            disableProtectedModulesWithNotice();
        }
        this.wasAuthorized = nowAuthorized;
    }

    private void disableProtectedModulesWithNotice() {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return;
        }
        int disabled = instance.getModuleManager().disableProtectedModules();
        if (disabled <= 0) return;
        class_310 mc = class_310.method_1551();
        if (mc != null && mc.field_1724 != null) {
            mc.field_1724.method_7353(class_2561.method_43470(("\u00a7d[VC]\u00a7c License lost. Disabled " + disabled + " protected modules.")), false);
        }
    }

    private boolean canUseOfflineCache() {
        return false;
    }

    private boolean hasSavedKey() {
        if (this.keyCode == null) break label_1;
        return !(this.keyCode.isBlank());
    }

    private void loadLocalState() {
    }

    private void persistLocalState() {
    }

    private boolean writeAuthStateFile(Path target, JsonObject root) {
        return false;
    }

    private JsonObject encryptKeyForStorage(String plainKey) throws Exception {
        byte[] salt = new byte[16];
        byte[] iv = new byte[12];
        RNG.nextBytes(salt);
        RNG.nextBytes(iv);
        SecretKey secretKey = deriveStorageKey(salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(1, secretKey, new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(plainKey.getBytes(StandardCharsets.UTF_8));
        JsonObject obj = new JsonObject();
        obj.addProperty("v", 1);
        obj.addProperty("salt", Base64.getEncoder().encodeToString(salt));
        obj.addProperty("iv", Base64.getEncoder().encodeToString(iv));
        obj.addProperty("ciphertext", Base64.getEncoder().encodeToString(encrypted));
        return obj;
    }

    private String decryptStoredKey(JsonObject encrypted) throws Exception {
        String saltB64 = getString(encrypted, "salt", "");
        String ivB64 = getString(encrypted, "iv", "");
        String ciphertextB64 = getString(encrypted, "ciphertext", "");
        if (((saltB64.isBlank()) || (ivB64.isBlank())) || (ciphertextB64.isBlank())) {
            return "";
        }
        byte[] salt = Base64.getDecoder().decode(saltB64);
        byte[] iv = Base64.getDecoder().decode(ivB64);
        byte[] ciphertext = Base64.getDecoder().decode(ciphertextB64);
        SecretKey secretKey = deriveStorageKey(salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(2, secretKey, new GCMParameterSpec(128, iv));
        byte[] plain = cipher.doFinal(ciphertext);
        return new String(plain, StandardCharsets.UTF_8);
    }

    private SecretKey deriveStorageKey(byte[] salt) throws Exception {
        char[] passphrase = buildStoragePassphrase().toCharArray();
        PBEKeySpec spec = new PBEKeySpec(passphrase, salt, 120000, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] raw = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(raw, "AES");
    }

    private String buildStoragePassphrase() {
        return (this.hwid + "|" + System.getProperty("user.name", "unknown") + "|" + System.getProperty("os.name", "unknown") + "|viecheats-auth-cache-v2");
    }

    private String buildChimeraProbeHeader() {
        if (this.networkChimeraEngine == null) {
            return "";
            try {
            }
            String probe = (Long.toString(System.currentTimeMillis()) + "|" + this.hwid.substring(0, Math.min(8, this.hwid.length())));
            return this.networkChimeraEngine.encodeToCompact(probe);
        } catch (Exception ex4) {
            Exception e = ex4;
            VieCheatsMod.LOGGER.warn("[VieCheat] Network Chimera probe header build failed: {}", e.getMessage());
            return "";
        }
    }

    private String computeCacheSignature() {
        return "";
    }

    private static String describeException(Throwable throwable) {
        if (throwable == null) return "unknown_error";
        String msg = throwable.getMessage();
        if (msg != null && !(msg.isBlank())) return msg;
        return throwable.getClass().getSimpleName();
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null) {
        } else {
        }
        byte[] left = a.getBytes(StandardCharsets.UTF_8);
        if (b == null) {
        } else {
        }
        byte[] right = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(left, right);
    }

    private AuthManager.SignedSessionClaims verifySignedSessionToken(String token, String expectedKey, String expectedHwid) throws Exception {
        if (this.verifierPublicKey == null) throw new IllegalStateException("No auth signature public key configured.");
        String[] parts = token.split("\\.", 2);
        if (parts.length != 2) throw new IllegalArgumentException("Malformed token.");
        byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[0]);
        byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[1]);
        Signature verifier = Signature.getInstance("Ed25519");
        verifier.initVerify(this.verifierPublicKey);
        verifier.update(payloadBytes);
        if (!(verifier.verify(signatureBytes))) throw new IllegalArgumentException("Token signature mismatch.");
        JsonObject payload = JsonParser.parseString(new String(payloadBytes, StandardCharsets.UTF_8)).getAsJsonObject();
        String payloadKey = normalizeKeyCode(getString(payload, "key", getString(payload, "sub", "")));
        String payloadHwid = getString(payload, "hwid", "").trim().toLowerCase(Locale.ROOT);
        long expEpoch = getLong(payload, "exp", 0L);
        long iatEpoch = getLong(payload, "iat", 0L);
        if ((payloadKey.isBlank()) || (!(payloadKey.equals(normalizeKeyCode(expectedKey))))) {
            throw new IllegalArgumentException("Token key claim mismatch.");
        }
        if ((payloadHwid.isBlank()) || (!(payloadHwid.equals(expectedHwid.trim().toLowerCase(Locale.ROOT))))) {
            throw new IllegalArgumentException("Token hwid claim mismatch.");
        }
        if (expEpoch <= 0L) throw new IllegalArgumentException("Token exp claim missing.");
        Instant now = Instant.now();
        Instant expiresAt = Instant.ofEpochSecond(expEpoch);
        if (now.isAfter(expiresAt.plusSeconds(30L))) throw new IllegalArgumentException("Token is expired.");
        if (iatEpoch > 0L) {
            Instant issuedAt = Instant.ofEpochSecond(iatEpoch);
            if (issuedAt.isAfter(now.plusSeconds(120L))) throw new IllegalArgumentException("Token issued-at is in the future.");
        }
        return new AuthManager.SignedSessionClaims(expiresAt);
    }

    private static PublicKey parseConfiguredPublicKey(String configuredKey) {
        if (configuredKey != null) {
            if (configuredKey.isBlank()) {
            }
            return null;
            try {
            }
            byte[] keyBytes = decodeBase64Flexible(configuredKey);
            if (keyBytes.length == 32) {
                byte[] wrapped = new byte[(ED25519_SPKI_PREFIX.length + keyBytes.length)];
                System.arraycopy(ED25519_SPKI_PREFIX, 0, wrapped, 0, ED25519_SPKI_PREFIX.length);
                System.arraycopy(keyBytes, 0, wrapped, ED25519_SPKI_PREFIX.length, keyBytes.length);
                keyBytes = wrapped;
            }
            KeyFactory factory = KeyFactory.getInstance("Ed25519");
            return factory.generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception ex11) {
            Exception e = ex11;
            VieCheatsMod.LOGGER.warn("[VieCheat] Invalid auth signing public key: {}", e.getMessage());
            return null;
        }
    }

    private static byte[] decodeBase64Flexible(String raw) {
        String value = raw.trim();
        if (value.startsWith("-----BEGIN")) {
            value = value.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replaceAll("\\s+", "");
            try {
            }
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException ex8) {
            IllegalArgumentException ignored = ex8;
            return Base64.getUrlDecoder().decode(value);
        }
    }

    private static JsonObject parseResponseBody(String body) {
        try {
            return JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception ex2) {
            Exception ignored = ex2;
            return new JsonObject();
        }
    }

    private static JsonObject getObject(JsonObject root, String key) {
        if (((root == null) || (!(root.has(key)))) || (!(root.get(key).isJsonObject()))) {
            return null;
        }
        return root.getAsJsonObject(key);
    }

    private static String getString(JsonObject root, String key, String fallback) {
        if ((root == null) || (!(root.has(key)))) break label_1;
        if (root.get(key).isJsonNull()) {
            label_1:
            return fallback;
            try {
            }
            String value = root.get(key).getAsString();
            if (value == null) {
                return fallback;
                try {
                }
                String trimmed = value.trim();
                if (trimmed.isEmpty()) {
                    return fallback;
                    try {
                    }
                    if (!"null".equalsIgnoreCase(trimmed)) {
                        if (!(!"undefined".equalsIgnoreCase(trimmed))) {
                        }
                        return fallback;
                        try {
                        }
                        return value;
                    } catch (Exception ex13) {
                        Exception ignored = ex13;
                        return fallback;
                    }
                }
            }
        }
    }

    private static String resolveRemoteMessage(JsonObject root, boolean ok, String remoteStatus) {
        String fallback = defaultMessageForRemoteStatus(ok, remoteStatus);
        String fromMessage = getString(root, "message", null);
        if (fromMessage != null) return fromMessage;
        String fromDetail = getString(root, "detail", null);
        if (fromDetail != null) return fromDetail;
        String fromError = getString(root, "error", null);
        if (fromError != null) return fromError;
        return fallback;
    }

    private static String defaultMessageForRemoteStatus(boolean ok, String remoteStatus) {
        if (!(!ok)) return "Key accepted.";
        if (remoteStatus != null) break label_2;
        break label_3;
        label_2:
        label_3:
        String normalized = remoteStatus.trim().toLowerCase(Locale.ROOT);
        Object var3 = normalized;
        int var4 = -1;
        switch (var3.hashCode()) {
            case -2054838772 -> { break label_11; }
            case -1625724613 -> { break label_10; }
            case -1309235419 -> { break label_5; }
            case -1268070397 -> { break label_7; }
            case -995425278 -> { break label_8; }
            case 58505192 -> { break label_9; }
            case 1100137118 -> { break label_6; }
            default -> { break label_12; }
        }
        label_5:
        if (!(var3.equals("expired"))) break label_12;
        var4 = 0;
        break label_12;
        label_6:
        if (!(var3.equals("revoked"))) break label_12;
        var4 = 1;
        break label_12;
        label_7:
        if (!(var3.equals("hwid_mismatch"))) break label_12;
        var4 = 2;
        break label_12;
        label_8:
        if (!(var3.equals("outdated_client"))) break label_12;
        var4 = 3;
        break label_12;
        label_9:
        if (!(var3.equals("outdated"))) break label_12;
        var4 = 4;
        break label_12;
        label_10:
        if (!(var3.equals("rate_limited"))) break label_12;
        var4 = 5;
        break label_12;
        label_11:
        if (!(var3.equals("server_error"))) break label_12;
        var4 = 6;
        label_12:
        switch (var4) {
            case 0 -> { break label_13; }
            case 1 -> { break label_14; }
            case 2 -> { break label_15; }
            case 3 -> { break label_16; }
            case 4 -> { break label_16; }
            case 5 -> { break label_17; }
            case 6 -> { break label_18; }
            default -> { break label_19; }
        }
        label_13:
        break label_20;
        label_14:
        break label_20;
        label_15:
        break label_20;
        label_16:
        break label_20;
        label_17:
        break label_20;
        label_18:
        break label_20;
        label_19:
        label_20:
        return "Authentication failed.";
    }

    private static boolean getBoolean(JsonObject root, String key, boolean fallback) {
        if (root != null && root.has(key)) {
            if (root.get(key).isJsonNull()) {
            }
            return fallback;
            try {
            }
            return root.get(key).getAsBoolean();
        } catch (Exception ex4) {
            Exception ignored = ex4;
            return fallback;
        }
    }

    private static long getLong(JsonObject root, String key, long fallback) {
        if (root != null && root.has(key)) {
            if (root.get(key).isJsonNull()) {
            }
            return fallback;
            try {
            }
            return root.get(key).getAsLong();
        } catch (Exception ex4) {
            Exception ignored = ex4;
            return fallback;
        }
    }

    private static Instant parseInstantOrNull(String value) {
        if (value != null) {
            if (value.isBlank()) {
            }
            return null;
            try {
            }
            return Instant.parse(value);
        } catch (DateTimeParseException ex4) {
            DateTimeParseException ignored = ex4;
            return null;
        }
    }

    private static String normalizeKeyCode(String raw) {
        if (raw == null) return "";
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private static AuthManager.Status parseStatus(String raw) {
        if (raw == null) return AuthManager.Status.MISSING_KEY;
        Object var1 = raw.trim().toLowerCase(Locale.ROOT);
        int var2 = -1;
        switch (var1.hashCode()) {
            case -2054838772 -> { break label_8; }
            case -1625724613 -> { break label_9; }
            case -1309235419 -> { break label_3; }
            case -1268070397 -> { break label_5; }
            case -995425278 -> { break label_6; }
            case 58505192 -> { break label_7; }
            case 111972348 -> { break label_2; }
            case 1100137118 -> { break label_4; }
            case 1348480006 -> { break label_10; }
            default -> { break label_11; }
        }
        label_2:
        if (!(var1.equals("valid"))) break label_11;
        var2 = 0;
        break label_11;
        label_3:
        if (!(var1.equals("expired"))) break label_11;
        var2 = 1;
        break label_11;
        label_4:
        if (!(var1.equals("revoked"))) break label_11;
        var2 = 2;
        break label_11;
        label_5:
        if (!(var1.equals("hwid_mismatch"))) break label_11;
        var2 = 3;
        break label_11;
        label_6:
        if (!(var1.equals("outdated_client"))) break label_11;
        var2 = 4;
        break label_11;
        label_7:
        if (!(var1.equals("outdated"))) break label_11;
        var2 = 5;
        break label_11;
        label_8:
        if (!(var1.equals("server_error"))) break label_11;
        var2 = 6;
        break label_11;
        label_9:
        if (!(var1.equals("rate_limited"))) break label_11;
        var2 = 7;
        break label_11;
        label_10:
        if (!(var1.equals("missing_key"))) break label_11;
        var2 = 8;
        label_11:
        switch (var2) {
            case 0 -> { break label_12; }
            case 1 -> { break label_13; }
            case 2 -> { break label_14; }
            case 3 -> { break label_15; }
            case 4 -> { break label_16; }
            case 5 -> { break label_16; }
            case 6 -> { break label_17; }
            case 7 -> { break label_18; }
            case 8 -> { break label_19; }
            default -> { break label_20; }
        }
        label_12:
        break label_21;
        label_13:
        break label_21;
        label_14:
        break label_21;
        label_15:
        break label_21;
        label_16:
        break label_21;
        label_17:
        break label_21;
        label_18:
        break label_21;
        label_19:
        break label_21;
        label_20:
        label_21:
        return AuthManager.Status.INVALID;
    }

    private static AuthManager.Status mapRemoteStatus(String raw) {
        if (raw == null) return AuthManager.Status.INVALID;
        Object var1 = raw.trim().toLowerCase(Locale.ROOT);
        int var2 = -1;
        switch (var1.hashCode()) {
            case -2054838772 -> { break label_7; }
            case -1625724613 -> { break label_8; }
            case -1309235419 -> { break label_2; }
            case -1268070397 -> { break label_4; }
            case -995425278 -> { break label_5; }
            case 58505192 -> { break label_6; }
            case 1100137118 -> { break label_3; }
            case 1348480006 -> { break label_10; }
            case 1615526678 -> { break label_11; }
            case 1705628455 -> { break label_9; }
            case 2117379143 -> { break label_12; }
            default -> { break label_13; }
        }
        label_2:
        if (!(var1.equals("expired"))) break label_13;
        var2 = 0;
        break label_13;
        label_3:
        if (!(var1.equals("revoked"))) break label_13;
        var2 = 1;
        break label_13;
        label_4:
        if (!(var1.equals("hwid_mismatch"))) break label_13;
        var2 = 2;
        break label_13;
        label_5:
        if (!(var1.equals("outdated_client"))) break label_13;
        var2 = 3;
        break label_13;
        label_6:
        if (!(var1.equals("outdated"))) break label_13;
        var2 = 4;
        break label_13;
        label_7:
        if (!(var1.equals("server_error"))) break label_13;
        var2 = 5;
        break label_13;
        label_8:
        if (!(var1.equals("rate_limited"))) break label_13;
        var2 = 6;
        break label_13;
        label_9:
        if (!(var1.equals("not_redeemed"))) break label_13;
        var2 = 7;
        break label_13;
        label_10:
        if (!(var1.equals("missing_key"))) break label_13;
        var2 = 8;
        break label_13;
        label_11:
        if (!(var1.equals("not_found"))) break label_13;
        var2 = 9;
        break label_13;
        label_12:
        if (!(var1.equals("invalid_request"))) break label_13;
        var2 = 10;
        label_13:
        switch (var2) {
            case 0 -> { break label_14; }
            case 1 -> { break label_15; }
            case 2 -> { break label_16; }
            case 3 -> { break label_17; }
            case 4 -> { break label_17; }
            case 5 -> { break label_18; }
            case 6 -> { break label_19; }
            case 7 -> { break label_20; }
            case 8 -> { break label_20; }
            case 9 -> { break label_20; }
            case 10 -> { break label_21; }
            default -> { break label_22; }
        }
        label_14:
        break label_23;
        label_15:
        break label_23;
        label_16:
        break label_23;
        label_17:
        break label_23;
        label_18:
        break label_23;
        label_19:
        break label_23;
        label_20:
        break label_23;
        label_21:
        break label_23;
        label_22:
        label_23:
        return AuthManager.Status.INVALID;
    }

    private static String resolveApiBaseUrl(String fileValue) {
        String fromEnv = System.getenv("VIE_AUTH_URL");
        if (fromEnv != null && !(fromEnv.isBlank())) return normalizeBaseUrl(fromEnv);
        if (fileValue != null && !(fileValue.isBlank())) return normalizeBaseUrl(fileValue);
        return "https://vieservices.top/api";
    }

    private static String normalizeBaseUrl(String url) {
        if (url != null) break label_1;
        break label_2;
        label_1:
        label_2:
        String normalized = url.trim();
        if (normalized.isBlank()) return "https://vieservices.top/api";
        if (normalized.contains("://")) break label_6;
        normalized = ("https://" + normalized);
        label_6:
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, (normalized.length() - 1));
            break label_6;
            try {
            }
            URI uri = URI.create(normalized);
            if (uri.getScheme() == null) {
            } else {
            }
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            String host = uri.getHost();
            int port = uri.getPort();
            if (uri.getPath() == null) {
            } else {
            }
            String path = uri.getPath().trim();
            if (!(path.isEmpty())) {
                if (!"/".equals(path)) break label_19;
            }
            path = "/api";
            while (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, (path.length() - 1));
            }
            if (host != null) {
                if (host.isBlank()) {
                }
                break label_25;
            }
            if (port > 0) {
                break label_25;
            }
            label_25:
            String authority = host;
            if (authority != null) {
                if (authority.isBlank()) {
                }
                return "https://vieservices.top/api";
                try {
                }
                return (scheme + "://" + authority + path);
            } catch (Exception ex31) {
                Exception ignored = ex31;
                return "https://vieservices.top/api";
            }
        }
    }

    private boolean isApiTransportAllowed(String baseUrl) {
        try {
            URI uri = URI.create(baseUrl);
            if (uri.getScheme() == null) {
            } else {
            }
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            if (uri.getHost() == null) {
            } else {
            }
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            if (!(!"https".equals(scheme))) {
                return true;
                try {
                }
                if (!(!"http".equals(scheme)) && isLoopbackHost(host)) {
                    return true;
                    try {
                    }
                    return this.allowInsecureHttp;
                } catch (Exception ex13) {
                    Exception ignored = ex13;
                    return false;
                }
            }
        }
    }

    private static boolean isPlaintextCompatAllowed(String baseUrl) {
        try {
            URI uri = URI.create(baseUrl);
            if (uri.getHost() == null) {
            } else {
            }
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            if (!"vieservices.top".equals(host)) {
                if (host.endsWith(".vieservices.top")) {
                }
            } else {
            }
            return false;
        } catch (Exception ex8) {
            Exception ignored = ex8;
            return false;
        }
    }

    private static boolean isLoopbackHost(String host) {
        if (!"localhost".equals(host) && !"127.0.0.1".equals(host) && !"::1".equals(host)) {
            if (!(!"[::1]".equals(host))) {
            }
        } else {
        }
        return false;
    }

    private static String resolveAuthSigningPublicKey() {
        String fromEnv = System.getenv("VIE_AUTH_PUBLIC_KEY");
        if (fromEnv != null && !(fromEnv.isBlank())) return fromEnv.trim();
        return "";
    }

    private static boolean resolveRequireSignedSessions(String publicKey) {
        String fromEnv = System.getenv("VIE_AUTH_REQUIRE_SIGNED");
        if (fromEnv != null) return Boolean.parseBoolean(fromEnv);
        return !(publicKey.isBlank());
    }

    private static boolean resolveAllowInsecureHttp() {
        String fromEnv = System.getenv("VIE_AUTH_ALLOW_INSECURE_HTTP");
        if (fromEnv != null) return Boolean.parseBoolean(fromEnv);
        return false;
    }

    private static String computeMachineFingerprint() {
        try {
            boolean isAndroid = isAndroidDevice();
            if (!(!isAndroid)) {
                String material = computeAndroidFingerprint();
            } else {
                var tmp0 = new CharSequence[7];
                tmp0[0] = System.getProperty("user.name", "unknown");
                tmp0[1] = System.getProperty("os.name", "unknown");
                tmp0[2] = System.getProperty("os.arch", "unknown");
                tmp0[3] = ((CharSequence) System.getenv().getOrDefault("COMPUTERNAME", ""));
                tmp0[4] = ((CharSequence) System.getenv().getOrDefault("PROCESSOR_IDENTIFIER", ""));
                tmp0[5] = ((CharSequence) System.getenv().getOrDefault("PROCESSOR_REVISION", ""));
                tmp0[6] = readWindowsMachineGuid();
                material = String.join("|", tmp0);
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(material.getBytes(StandardCharsets.UTF_8));
            String result = HexFormat.of().formatHex(hash);
            VieCheatsMod.LOGGER.debug("[HWID] Fingerprint computed: {}", result);
            return result;
        } catch (NoSuchAlgorithmException ex19) {
            NoSuchAlgorithmException e = ex19;
            return "unknown-hwid";
        }
    }

    private static boolean isAndroidDevice() {
        String vendor = System.getProperty("java.vm.vendor", "").toLowerCase(Locale.ROOT);
        String name = System.getProperty("java.vm.name", "").toLowerCase(Locale.ROOT);
        String osVersion = System.getProperty("os.version", "").toLowerCase(Locale.ROOT);
        String userHome = System.getProperty("user.home", "").toLowerCase(Locale.ROOT);
        if (!(vendor.contains("android")) && !(name.contains("android")) && !(vendor.contains("dalvik")) && !(osVersion.contains("android"))) {
            if (userHome.contains("android")) {
            }
        } else {
        }
        return false;
    }

    private static String computeAndroidFingerprint() {
        StringBuilder sb = new StringBuilder();
        sb.append(readOrCreateAndroidUuid());
        sb.append("|").append(System.getProperty("os.name", ""));
        sb.append("|").append(System.getProperty("os.arch", ""));
        sb.append("|").append(System.getProperty("os.version", ""));
        sb.append("|").append(readBuildProp("ro.product.model"));
        sb.append("|").append(System.getProperty("user.home", ""));
        sb.append("|").append(readAndroidId());
        sb.append("|").append(readFirstLine("/proc/cpuinfo"));
        sb.append("|").append(readFirstLine("/proc/version"));
        VieCheatsMod.LOGGER.debug("[HWID] Android fingerprint material length: {}", sb.length());
        return sb.toString();
    }

    private static String readOrCreateAndroidUuid() {
        try {
            String home = System.getProperty("user.home", "");
            String tmpdir = System.getProperty("java.io.tmpdir", "");
            var tmp0 = new Path[2];
            var tmp1 = new String[1];
            tmp1[0] = ".vhwid";
            tmp0[0] = Path.of(home, tmp1);
            var tmp2 = new String[1];
            tmp2[0] = ".vhwid";
            tmp0[1] = Path.of(tmpdir, tmp2);
            Path[] candidates = tmp0;
            String newUuid = candidates;
            int var4 = newUuid.length;
            int var5 = 0;
            while (var5 < var4) {
                Path path = newUuid[var5];
                try {
                    if (!(Files.exists(path, new LinkOption[0]))) break label_12;
                    String uuid = Files.readString(path).trim();
                    if ((uuid.length() != 36) || (!(uuid.contains("-")))) break label_12;
                    return uuid;
                    try {
                        label_12:
                    } catch (Exception ex13) {
                        uuid = ex13;
                    }
                    var5++;
                }
                newUuid = UUID.randomUUID().toString();
                var4 = candidates;
                var5 = var4.length;
                path = 0;
                while (path < var5) {
                    path = var4[path];
                    try {
                        Files.writeString(path, newUuid, new OpenOption[0]);
                    } catch (Exception ex20) {
                        Object var8 = ex20;
                        path++;
                    }
                }
                label_22:
                return newUuid;
            } catch (Exception ex24) {
                Exception e = ex24;
                return UUID.randomUUID().toString();
            }
        }
    }

    private static String readAndroidId() {
        try {
            Class secureClass = Class.forName("android.provider.Settings$Secure");
            Class contentResolverClass = Class.forName("android.content.ContentResolver");
            Class atClass = Class.forName("android.app.ActivityThread");
            Object app = atClass.getMethod("currentApplication", new Class[0]).invoke(null, new Object[0]);
            if (app == null) {
                return "";
                try {
                }
                Object resolver = app.getClass().getMethod("getContentResolver", new Class[0]).invoke(app, new Object[0]);
                if (resolver == null) {
                    return "";
                    try {
                    }
                    var tmp0 = new Class[2];
                    tmp0[0] = contentResolverClass;
                    tmp0[1] = String.class;
                    var tmp1 = new Object[2];
                    tmp1[0] = resolver;
                    tmp1[1] = "android_id";
                    Object androidId = secureClass.getMethod("getString", tmp0).invoke(null, tmp1);
                    if (androidId != null) {
                        String id = androidId.toString();
                        if (id != null && !(id.equals("null")) && !(id.isEmpty())) return id;
                    }
                } catch (Exception ex17) {
                    secureClass = ex17;
                }
                return "";
            }
        }
    }

    private static String readBuildProp(String key) {
        try {
            var tmp0 = new String[2];
            tmp0[0] = "getprop";
            tmp0[1] = key;
            Process p = new ProcessBuilder(tmp0).redirectErrorStream(true).start();
            String val = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            p.destroy();
            return val;
        } catch (Exception ex6) {
            Exception e = ex6;
            return "";
        }
    }

    private static String readFirstLine(String filePath) {
        try {
            return Files.readString(Path.of(filePath, new String[0])).split("\\R")[0].trim();
        } catch (Exception ex2) {
            Exception e = ex2;
            return "";
        }
    }

    private static String readWindowsMachineGuid() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!(os.contains("windows"))) {
            return "";
            try {
            }
            var tmp0 = new String[5];
            tmp0[0] = "reg";
            tmp0[1] = "query";
            tmp0[2] = "HKLM\\SOFTWARE\\Microsoft\\Cryptography";
            tmp0[3] = "/v";
            tmp0[4] = "MachineGuid";
            Process process = new ProcessBuilder(tmp0).redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Object var3 = output.split("\\R");
            int var4 = var3.length;
            int var5 = 0;
            while (var5 < var4) {
                String line = var3[var5];
                String trimmed = line.trim();
                if (!(trimmed.toLowerCase(Locale.ROOT).contains("machineguid"))) {
                    break label_13;
                }
                String[] parts = trimmed.split("\\s+");
                if (parts.length == 0) {
                    break label_13;
                }
                return parts[(parts.length - 1)].trim();
                try {
                    label_13:
                    var5++;
                }
            } catch (Exception ex15) {
                process = ex15;
            }
            return "";
        }
    }

    static {
        GSON = new GsonBuilder().setPrettyPrinting().create();
        CONNECT_TIMEOUT = Duration.ofSeconds(4L);
        REQUEST_TIMEOUT = Duration.ofSeconds(6L);
        RNG = new SecureRandom();
        ED25519_SPKI_PREFIX = HexFormat.of().parseHex("302a300506032b6570032100");
        KEY_CODE_PATTERN = Pattern.compile("(?i)\\bVIE-[A-Z0-9]+-[A-F0-9]{6}\\b");
    }


    record AuthFileCandidate(Path path, long modifiedMs, boolean hasKey) {

        private AuthFileCandidate(Path path, long modifiedMs, boolean hasKey) {
            this.path = path;
            this.modifiedMs = modifiedMs;
            this.hasKey = hasKey;
        }

        public Path path() {
            return this.path;
        }

        public long modifiedMs() {
            return this.modifiedMs;
        }

        public boolean hasKey() {
            return this.hasKey;
        }

    }

    record KeyBootstrapCandidate(String keyCode, long modifiedMs, Path sourcePath) {

        private KeyBootstrapCandidate(String keyCode, long modifiedMs, Path sourcePath) {
            this.keyCode = keyCode;
            this.modifiedMs = modifiedMs;
            this.sourcePath = sourcePath;
        }

        public String keyCode() {
            return this.keyCode;
        }

        public long modifiedMs() {
            return this.modifiedMs;
        }

        public Path sourcePath() {
            return this.sourcePath;
        }

    }

    record SignedSessionClaims(Instant expiresAt) {

        private SignedSessionClaims(Instant expiresAt) {
            this.expiresAt = expiresAt;
        }

        public Instant expiresAt() {
            return this.expiresAt;
        }

    }

    public record StatusSnapshot(boolean authorized, String status, String message, String keyCode, String hwid, String apiBaseUrl, String customerName, String plan, Instant expiresAt, long lastValidatedAtMs) {

        public StatusSnapshot(boolean authorized, String status, String message, String keyCode, String hwid, String apiBaseUrl, String customerName, String plan, Instant expiresAt, long lastValidatedAtMs) {
            this.authorized = authorized;
            this.status = status;
            this.message = message;
            this.keyCode = keyCode;
            this.hwid = hwid;
            this.apiBaseUrl = apiBaseUrl;
            this.customerName = customerName;
            this.plan = plan;
            this.expiresAt = expiresAt;
            this.lastValidatedAtMs = lastValidatedAtMs;
        }

        public boolean authorized() {
            return this.authorized;
        }

        public String status() {
            return this.status;
        }

        public String message() {
            return this.message;
        }

        public String keyCode() {
            return this.keyCode;
        }

        public String hwid() {
            return this.hwid;
        }

        public String apiBaseUrl() {
            return this.apiBaseUrl;
        }

        public String customerName() {
            return this.customerName;
        }

        public String plan() {
            return this.plan;
        }

        public Instant expiresAt() {
            return this.expiresAt;
        }

        public long lastValidatedAtMs() {
            return this.lastValidatedAtMs;
        }

    }

    public enum Status {
        VALID,
        MISSING_KEY,
        INVALID,
        OUTDATED_CLIENT,
        EXPIRED,
        REVOKED,
        HWID_MISMATCH,
        RATE_LIMITED,
        SERVER_ERROR;


        private Status() {
        }

    }

    class 1 implements VieWebSocketClient.MessageHandler {

        1(AuthManager this$0) {
            this.this$0 = this$0;
        }

        public void onMessage(String message) {
        }

        public void onKeyExpired(String keyCode) {
            VieCheatsMod.LOGGER.error("[VieCheat] WebSocket: Key expired notification received.");
            if (keyCode.equals(this.this$0.keyCode)) {
                this.this$0.forceLockdown("Key expired via real-time notification");
            }
        }

        public void onKeyExpiringSoon(String keyCode, int hoursLeft) {
            VieCheatsMod.LOGGER.warn("[VieCheat] WebSocket: Key expiring in {} hours.", hoursLeft);
            if (!(keyCode.equals(this.this$0.keyCode))) break label_5;
            class_310 mc = class_310.method_1551();
            if ((mc == null) || (mc.field_1724 == null)) break label_5;
            mc.field_1724.method_7353(class_2561.method_43470(("\u00a7d[VC]\u00a7e License expiring in " + hoursLeft + " hours. Renew to avoid interruption.")), false);
            label_5:
        }

        public void onKeyRevoked(String keyCode, String reason) {
            VieCheatsMod.LOGGER.error("[VieCheat] WebSocket: Key revoked notification received. Reason: {}", reason);
            if (keyCode.equals(this.this$0.keyCode)) {
                this.this$0.forceLockdown(("Key revoked via real-time notification: " + reason));
            }
        }

        public void onKeyUnrevoked(String keyCode) {
            if (keyCode.equals(this.this$0.keyCode)) {
                this.this$0.requestImmediateValidation();
            }
        }

        public void onKeyExtended(String keyCode, String newExpiresAt) {
            if (keyCode.equals(this.this$0.keyCode)) {
                this.this$0.requestImmediateValidation();
            }
        }

        public void onHwidReset(String keyCode) {
            VieCheatsMod.LOGGER.warn("[VieCheat] WebSocket: HWID reset notification received.");
            if (keyCode.equals(this.this$0.keyCode)) {
                this.this$0.requestImmediateValidation();
            }
        }

        public void onKeyDeleted(String keyCode) {
            VieCheatsMod.LOGGER.error("[VieCheat] WebSocket: Key deleted notification received.");
            if (keyCode.equals(this.this$0.keyCode)) {
                this.this$0.forceLockdown("Key deleted via real-time notification");
            }
        }

    }
}
