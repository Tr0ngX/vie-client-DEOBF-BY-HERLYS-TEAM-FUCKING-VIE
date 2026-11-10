package me.drake.viecheats.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocketHandshakeException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import me.drake.viecheats.security.ChimeraEngine;
import net.minecraft.class_310;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VieWebSocketClient {
    private static final Logger LOGGER;
    private static final int MAX_RECONNECT_ATTEMPTS = 8;
    private final String serverUrl;
    private final String keyCode;
    private final String hwid;
    private final ChimeraEngine chimeraEngine;
    private final WebSocketLogger wsLogger;
    private volatile WebSocket wsClient;
    private volatile boolean connected;
    private volatile boolean connecting;
    private volatile boolean reconnectEnabled;
    private volatile boolean closingByClient;
    private final ScheduledExecutorService executorService;
    private final ScheduledExecutorService pingExecutor;
    private volatile ScheduledFuture pingTask;
    private final AtomicInteger reconnectAttempts;
    private volatile long connectionStartTime;
    private VieWebSocketClient.MessageHandler messageHandler;

    public VieWebSocketClient(String serverUrl, String keyCode, String hwid, String chimeraKey) {
        this.connected = false;
        this.connecting = false;
        this.reconnectEnabled = true;
        this.closingByClient = false;
        this.reconnectAttempts = new AtomicInteger(0);
        this.connectionStartTime = 0L;
        this.serverUrl = serverUrl;
        this.keyCode = keyCode;
        this.hwid = hwid;
        this.chimeraEngine = new ChimeraEngine(chimeraKey);
        this.executorService = Executors.newSingleThreadScheduledExecutor((r) -> {
    Thread thread = new Thread(r, "WebSocket-Reconnect");
    thread.setDaemon(true);
    return thread;
});
        this.pingExecutor = Executors.newSingleThreadScheduledExecutor((r) -> {
    Thread thread = new Thread(r, "WebSocket-Ping");
    thread.setDaemon(true);
    return thread;
});
        Path mcDir = class_310.method_1551().field_1697.toPath();
        this.wsLogger = new WebSocketLogger(mcDir);
        var tmp0 = new Object[2];
        if (keyCode != null && keyCode.length() > 8) {
        } else {
        }
        0[keyCode.substring(0, 4)] = "null";
        if (keyCode != null && keyCode.length() > 8) {
        } else {
        }
        1[keyCode.substring((keyCode.length() - 4))] = "";
        "WebSocketClient created: key=%s...%s".info(tmp0, String.format(tmp0, tmp0));
    }

    public void setMessageHandler(VieWebSocketClient.MessageHandler handler) {
        this.messageHandler = handler;
    }

    public synchronized void connect() {
        this.wsLogger.info("CONNECT", "Connecting to server...");
        if (false) {
            if (!this.connecting) break label_3;
        }
        return;
        try {
            label_3:
            URI uri = new URI(this.serverUrl);
            if (uri.getScheme() != null) break label_5;
            break label_6;
            label_5:
            label_6:
            String scheme = uri.getScheme().toLowerCase();
            if (!"wss".equals(scheme)) {
                if (!(!"https".equals(scheme))) {
                }
            } else {
            }
            boolean isWSS = false;
            if (!"ws".equals(scheme)) {
                if (!(!"http".equals(scheme))) {
                }
            } else {
            }
            boolean isWS = false;
            HttpClient.Builder clientBuilder = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L));
            if (!isWSS) break label_30;
            this.wsLogger.info("PROTOCOL", "Using WebSocket Secure (WSS) - TLS encrypted");
            this.wsLogger.ssl("INIT", "WSS connection requested, setting up SSL context");
            try {
                SSLContext sslContext = buildStrictSslContext();
                clientBuilder.sslContext(sslContext);
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                this.wsLogger.ssl("SUCCESS", "SSL certificate validation enabled with StrictX509TrustManager");
            } catch (Exception ex25) {
                Exception e = ex25;
                var tmp0 = new Object[1];
                tmp0[0] = e.getMessage();
                this.wsLogger.ssl("FAILED", String.format("Failed to initialize SSL context: %s", tmp0));
                LOGGER.error("[WebSocket] Failed to initialize SSL context: {}", e.getMessage());
                this.wsLogger.error("CONNECT", "SSL setup failed, aborting connection", e);
                return;
                try {
                    label_30:
                    if (!(!isWS)) {
                        this.wsLogger.warn("SECURITY", "WARNING: Using non-secure WebSocket (WS). Data is plaintext.");
                        LOGGER.warn("[WebSocket] Using non-secure WebSocket (ws://)");
                        break label_35;
                    }
                    this.wsLogger.error("PROTOCOL", "Unknown protocol in URL");
                    throw new IllegalArgumentException("URL must start with ws://, wss://, http://, or https://");
                }
                label_35:
                var tmp1 = new Object[1];
                tmp1[0] = (!(!isWSS) ? "WSS" : "WS");
                this.wsLogger.info("CONNECT", String.format("Protocol: %s, Starting connection...", tmp1));
                HttpClient httpClient = clientBuilder.build();
                VieWebSocketClient.ClientListener listener = new VieWebSocketClient.ClientListener(this);
                this.connecting = true;
                httpClient.newWebSocketBuilder().connectTimeout(Duration.ofSeconds(10L)).buildAsync(uri, listener).whenComplete((throwable) -> {
    if (throwable != null) {
        this.connecting = false;
        this.connected = false;
        this.wsClient = null;
        int handshakeStatus = extractHandshakeStatus(throwable);
        boolean fatalHandshake = isFatalHandshakeStatus(handshakeStatus);
        if (handshakeStatus > 0) {
            var tmp0 = new Object[1];
            tmp0[0] = handshakeStatus;
            this.wsLogger.warn("CONNECT", String.format("Handshake rejected with HTTP %d", tmp0));
        }
        if (!(!fatalHandshake)) {
            String reason = "WebSocket handshake rejected by server";
            var tmp1 = new Object[2];
            tmp1[0] = reason;
            tmp1[1] = handshakeStatus;
            this.wsLogger.warn("CONNECT", String.format("%s (HTTP %d)", tmp1));
            LOGGER.warn("[WebSocket] {} (HTTP {}).", reason, handshakeStatus);
        } else {
            var tmp2 = new Object[1];
            tmp2[0] = throwable.getMessage();
            this.wsLogger.error("CONNECT", String.format("Connection failed: %s", tmp2), throwable);
            LOGGER.error("[WebSocket] Connection failed: {}", throwable.getMessage(), throwable);
        }
        if (!(!this.reconnectEnabled)) {
            if (!(!fatalHandshake)) {
                this.reconnectEnabled = false;
                var tmp3 = new Object[1];
                tmp3[0] = handshakeStatus;
                this.wsLogger.warn("RECONNECT", String.format("Disabling reconnect for this session due to fatal handshake HTTP %d", tmp3));
                LOGGER.warn("[WebSocket] Fatal handshake HTTP {}. Reconnect disabled for this session.", handshakeStatus);
                return;
            }
            int attempt = this.reconnectAttempts.incrementAndGet();
            if (attempt > 8) {
                this.reconnectEnabled = false;
                var tmp4 = new Object[1];
                tmp4[0] = 8;
                this.wsLogger.warn("RECONNECT", String.format("Max reconnect attempts reached (%d). Reconnect disabled for this session.", tmp4));
                LOGGER.warn("[WebSocket] Max reconnect attempts reached ({}). Reconnect disabled.", 8);
                return;
            }
            var tmp5 = new Object[1];
            tmp5[0] = throwable.getMessage();
            this.wsLogger.reconnectAttempt(attempt, String.format("Connection failure: %s", tmp5));
            scheduleReconnect();
        }
        return;
    }
    this.wsClient = socket;
});
            } catch (URISyntaxException ex46) {
                e = ex46;
                this.wsLogger.error("CONNECT", "Invalid URI syntax", e);
                LOGGER.error("[WebSocket] Invalid server URL");
            } catch (Exception ex50) {
                e = ex50;
                var tmp2 = new Object[1];
                tmp2[0] = e.getMessage();
                this.wsLogger.error("CONNECT", String.format("Connection failed: %s", tmp2), e);
                LOGGER.error("[WebSocket] Connection failed");
            }
        }
        label_53:
    }

    private SSLContext buildStrictSslContext() throws GeneralSecurityException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        var tmp0 = new TrustManager[1];
        tmp0[0] = new VieWebSocketClient.StrictX509TrustManager();
        sslContext.init(null, tmp0, new SecureRandom());
        return sslContext;
    }

    private void handleDisconnect(int code, String reason, boolean remote, AtomicBoolean disconnectHandled) {
        if (!(disconnectHandled.compareAndSet(false, true))) return;
        if (this.connectionStartTime <= 0L) break label_3;
        break label_4;
        label_3:
        label_4:
        long duration = 0L;
        this.connected = false;
        this.connecting = false;
        this.wsClient = null;
        stopPing();
        var tmp0 = new Object[2];
        tmp0[0] = duration;
        tmp0[1] = remote;
        this.wsLogger.connection("CLOSED", this.serverUrl, String.format("duration=%dms, remote=%s", tmp0));
        var tmp1 = new Object[4];
        tmp1[0] = code;
        tmp1[1] = reason;
        tmp1[2] = remote;
        tmp1[3] = duration;
        this.wsLogger.warn("DISCONNECT", String.format("Connection closed: code=%d, reason=%s, remote=%s, duration=%dms", tmp1));
        LOGGER.warn("[WebSocket] Disconnected: {} (code: {})", reason, code);
        if (!(!this.reconnectEnabled)) {
            if (isFatalHandshakeStatus(code)) {
                this.reconnectEnabled = false;
                var tmp2 = new Object[1];
                tmp2[0] = code;
                this.wsLogger.warn("RECONNECT", String.format("Disabling reconnect due to fatal status code %d", tmp2));
                LOGGER.warn("[WebSocket] Fatal status code {}. Reconnect disabled.", code);
                return;
            }
            int attempt = this.reconnectAttempts.incrementAndGet();
            if (attempt > 8) {
                this.reconnectEnabled = false;
                var tmp3 = new Object[1];
                tmp3[0] = 8;
                this.wsLogger.warn("RECONNECT", String.format("Max reconnect attempts reached (%d). Reconnect disabled for this session.", tmp3));
                LOGGER.warn("[WebSocket] Max reconnect attempts reached ({}). Reconnect disabled.", 8);
                return;
            }
            var tmp4 = new Object[1];
            tmp4[0] = reason;
            this.wsLogger.reconnectAttempt(attempt, String.format("Previous connection closed: %s", tmp4));
            scheduleReconnect();
        }
    }

    private void authenticate() {
        try {
            this.wsLogger.auth("SENDING", this.keyCode, "Preparing authentication message");
            var tmp0 = new Object[2];
            tmp0[0] = this.keyCode;
            tmp0[1] = this.hwid;
            String authMessage = String.format("{\"type\":\"authenticate\",\"key\":\"%s\",\"hwid\":\"%s\"}", tmp0);
            if (this.keyCode != null && this.keyCode.length() >= 4) {
            } else {
            }
            String maskedKeyForLog = "****";
            sendEncrypted(authMessage);
            this.wsLogger.auth("SENT", this.keyCode, "Authentication message encrypted and sent");
        } catch (Exception ex9) {
            Exception e = ex9;
            var tmp1 = new Object[1];
            tmp1[0] = e.getMessage();
            this.wsLogger.error("AUTH", String.format("Failed to send auth: %s", tmp1), e);
            LOGGER.error("[WebSocket] Failed to send auth: {}", e.getMessage());
        }
    }

    private void handleMessage(String message) {
        try {
            if (!(message.contains("\"sequence\""))) break label_1;
            if (message.contains("\"locus\"")) break label_5;
            label_1:
            this.wsLogger.error("SECURITY", "Received unencrypted message - SECURITY VIOLATION");
            LOGGER.error("[WebSocket] SECURITY VIOLATION: Received unencrypted message");
            disconnect();
            return;
            try {
                label_5:
                String sequence = extractJsonValue(message, "sequence");
                String locus = extractJsonValue(message, "locus");
                if (!(sequence.isEmpty())) {
                    if (locus.isEmpty()) {
                    }
                    var tmp0 = new Object[2];
                    tmp0[0] = sequence.length();
                    tmp0[1] = locus.length();
                    this.wsLogger.error("DECRYPT", String.format("Empty sequence or locus: seq_len=%d, locus_len=%d", tmp0));
                    disconnect();
                    return;
                    try {
                    }
                    ChimeraEngine.GenomePacket packet = new ChimeraEngine.GenomePacket(sequence, locus);
                    String decryptedMessage = this.chimeraEngine.decode(packet);
                } catch (Exception ex16) {
                    Exception e = ex16;
                    var tmp1 = new Object[1];
                    tmp1[0] = e.getMessage();
                    this.wsLogger.error("DECRYPT", String.format("Failed to decrypt message: %s", tmp1), e);
                    LOGGER.error("[WebSocket] SECURITY VIOLATION: Failed to decrypt message: {}", e.getMessage());
                    disconnect();
                    return;
                    try {
                    }
                    this.wsLogger.message("DECRYPTED", "success", decryptedMessage);
                    if (this.messageHandler != null) {
                        this.messageHandler.onMessage(decryptedMessage);
                    }
                    if (!(decryptedMessage.contains("\"type\":"))) break label_67;
                    String type = extractJsonValue(decryptedMessage, "type");
                    locus = type;
                    packet = -1;
                    switch (locus.hashCode()) {
                        case -1713199559 -> { break label_33; }
                        case -277930683 -> { break label_27; }
                        case 84785945 -> { break label_31; }
                        case 394435386 -> { break label_32; }
                        case 480915358 -> { break label_28; }
                        case 1758344197 -> { break label_30; }
                        case 2131441854 -> { break label_29; }
                        default -> { break label_34; }
                    }
                    label_27:
                    if (!(locus.equals("key_expired"))) break label_34;
                    packet = 0;
                    break label_34;
                    label_28:
                    if (!(locus.equals("key_expiring_soon"))) break label_34;
                    packet = 1;
                    break label_34;
                    label_29:
                    if (!(locus.equals("key_revoked"))) break label_34;
                    packet = 2;
                    break label_34;
                    label_30:
                    if (!(locus.equals("key_unrevoked"))) break label_34;
                    packet = 3;
                    break label_34;
                    label_31:
                    if (!(locus.equals("key_extended"))) break label_34;
                    packet = 4;
                    break label_34;
                    label_32:
                    if (!(locus.equals("hwid_reset"))) break label_34;
                    packet = 5;
                    break label_34;
                    label_33:
                    if (!(locus.equals("key_deleted"))) break label_34;
                    packet = 6;
                    label_34:
                    switch (packet) {
                        case 0 -> { break label_35; }
                        case 1 -> { break label_41; }
                        case 2 -> { break label_45; }
                        case 3 -> { break label_52; }
                        case 4 -> { break label_55; }
                        case 5 -> { break label_59; }
                        case 6 -> { break label_62; }
                        default -> { break label_67; }
                    }
                    label_35:
                    String expiredKey = extractJsonValue(decryptedMessage, "keyCode");
                    this.wsLogger.info("EVENT", "Key expired notification received");
                    if (this.messageHandler != null) {
                        this.messageHandler.onKeyExpired(expiredKey);
                    }
                    LOGGER.warn("[WebSocket] Key expired notification received.");
                    break label_67;
                    label_41:
                    String expiringKey = extractJsonValue(decryptedMessage, "keyCode");
                    int hoursLeft = extractJsonInt(decryptedMessage, "hoursLeft");
                    if (this.messageHandler == null) break label_67;
                    this.messageHandler.onKeyExpiringSoon(expiringKey, hoursLeft);
                    break label_67;
                    label_45:
                    String revokedKey = extractJsonValue(decryptedMessage, "keyCode");
                    String revokeReason = extractJsonValue(decryptedMessage, "reason");
                    var tmp2 = new Object[1];
                    tmp2[0] = revokeReason;
                    this.wsLogger.warn("EVENT", String.format("Key revoked notification received, reason: %s", tmp2));
                    if (this.messageHandler != null) {
                        this.messageHandler.onKeyRevoked(revokedKey, revokeReason);
                    }
                    LOGGER.warn("[WebSocket] Key revoked notification received (reason: {}).", revokeReason);
                    break label_67;
                    label_52:
                    String unrevokedKey = extractJsonValue(decryptedMessage, "keyCode");
                    if (this.messageHandler == null) break label_67;
                    this.messageHandler.onKeyUnrevoked(unrevokedKey);
                    break label_67;
                    label_55:
                    String extendedKey = extractJsonValue(decryptedMessage, "keyCode");
                    String newExpiresAt = extractJsonValue(decryptedMessage, "newExpiresAt");
                    if (this.messageHandler == null) break label_67;
                    this.messageHandler.onKeyExtended(extendedKey, newExpiresAt);
                    break label_67;
                    label_59:
                    String resetKey = extractJsonValue(decryptedMessage, "keyCode");
                    if (this.messageHandler == null) break label_67;
                    this.messageHandler.onHwidReset(resetKey);
                    break label_67;
                    label_62:
                    String deletedKey = extractJsonValue(decryptedMessage, "keyCode");
                    if (this.messageHandler != null) {
                        this.messageHandler.onKeyDeleted(deletedKey);
                    }
                    LOGGER.warn("[WebSocket] Key deleted notification received.");
                    break label_67;
                    label_67:
                } catch (Exception ex68) {
                    e = ex68;
                    var tmp3 = new Object[1];
                    tmp3[0] = e.getMessage();
                    this.wsLogger.error("MSG", String.format("Failed to handle message: %s", tmp3), e);
                    LOGGER.error("[WebSocket] Failed to handle message: {}", e.getMessage());
                }
            }
        }
    }

    private void sendEncrypted(String message) {
        try {
            ChimeraEngine.GenomePacket packet = this.chimeraEngine.encode(message);
            var tmp0 = new Object[2];
            tmp0[0] = packet.sequence();
            tmp0[1] = packet.locus();
            String encrypted = String.format("{\"sequence\":\"%s\",\"locus\":\"%s\"}", tmp0);
            var tmp1 = new Object[2];
            tmp1[0] = packet.sequence().length();
            tmp1[1] = packet.locus().length();
            this.wsLogger.message("SEND", "encrypted", String.format("seq_len=%d, locus_len=%d", tmp1));
            WebSocket client = this.wsClient;
            if (client != null && !(!this.connected)) {
                client.sendText(encrypted, true).exceptionally(() -> {
    var tmp0 = new Object[1];
    tmp0[0] = error.getMessage();
    this.wsLogger.error("SEND", String.format("Failed to send message: %s", tmp0), error);
    return null;
});
            } else {
                this.wsLogger.error("SEND", "WebSocket client is not connected");
            }
        } catch (Exception ex12) {
            Exception e = ex12;
            var tmp2 = new Object[1];
            tmp2[0] = e.getMessage();
            this.wsLogger.error("SEND", String.format("Failed to encrypt and send: %s", tmp2), e);
            LOGGER.error("[WebSocket] Failed to encrypt and send: {}", e.getMessage());
        }
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = ("\"" + key + "\":");
        int start = json.indexOf(searchKey);
        if (start == -1) return "";
        start = (start + searchKey.length());
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        if (start < json.length() && json.charAt(start) == 34) {
            start++;
            int end = json.indexOf(34, start);
            if (end == -1) return "";
            return json.substring(start, end);
        }
        end = start;
        while (end < json.length() && json.charAt(end) != 44 && json.charAt(end) != 125) {
            end++;
        }
        return json.substring(start, end).trim();
    }

    private int extractJsonInt(String json, String key) {
        String value = extractJsonValue(json, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex3) {
            NumberFormatException e = ex3;
            return 0;
        }
    }

    private synchronized void startPing() {
        stopPing();
        this.pingTask = this.pingExecutor.scheduleAtFixedRate(() -> {
    if ((!this.connected) || (this.wsClient == null)) break label_5;
    try {
        sendEncrypted("{\"type\":\"ping\"}");
    } catch (Exception ex3) {
        Exception e = ex3;
        LOGGER.error("[WebSocket] Failed to send ping: {}", e.getMessage());
    }
    label_5:
}, 30L, 30L, TimeUnit.SECONDS);
    }

    private synchronized void stopPing() {
        ScheduledFuture task = this.pingTask;
        if (task != null) {
            task.cancel(false);
            this.pingTask = null;
        }
    }

    private void scheduleReconnect() {
        int attempt = this.reconnectAttempts.get();
        int delaySeconds = Math.min((5 + (attempt * 2)), 30);
        var tmp0 = new Object[2];
        tmp0[0] = attempt;
        tmp0[1] = delaySeconds;
        this.wsLogger.info("RECONNECT", String.format("Scheduling reconnect attempt #%d in %d seconds", tmp0));
        this.executorService.schedule(() -> {
    if (!this.connected && !this.connecting && !(!this.reconnectEnabled)) {
        var tmp0 = new Object[1];
        tmp0[0] = attempt;
        this.wsLogger.info("RECONNECT", String.format("Executing reconnect attempt #%d", tmp0));
        connect();
    }
}, ((long) delaySeconds), TimeUnit.SECONDS);
    }

    public synchronized void disconnect() {
        this.wsLogger.info("DISCONNECT", "Disconnecting WebSocket...");
        this.reconnectEnabled = false;
        this.connecting = false;
        stopPing();
        WebSocket client = this.wsClient;
        this.wsClient = null;
        if (client == null) break label_23;
        try {
            this.closingByClient = true;
            client.sendClose(1000, "client disconnect").orTimeout(3L, TimeUnit.SECONDS).exceptionally(() -> {
    var tmp0 = new Object[1];
    tmp0[0] = error.getMessage();
    this.wsLogger.error("DISCONNECT", String.format("Error closing WebSocket: %s", tmp0), error);
    return null;
});
            this.closingByClient = false;
        } catch (Exception ex13) {
            Exception e = ex13;
            var tmp0 = new Object[1];
            tmp0[0] = e.getMessage();
            this.wsLogger.error("DISCONNECT", String.format("Error closing WebSocket: %s", tmp0));
            try {
                client.abort();
            } catch (Exception ex17) {
                Object var3 = ex17;
            }
            this.closingByClient = false;
            break label_23;
            Object var4 = null;
            this.closingByClient = false;
            throw var4;
        }
        label_23:
        shutdownExecutor(this.executorService, "reconnect");
        shutdownExecutor(this.pingExecutor, "ping");
        this.connected = false;
        this.wsLogger.info("DISCONNECT", "WebSocket disconnect completed");
    }

    private void shutdownExecutor(ScheduledExecutorService executor, String name) {
        executor.shutdownNow();
        try {
            if (!(executor.awaitTermination(2L, TimeUnit.SECONDS))) {
                var tmp0 = new Object[1];
                tmp0[0] = name;
                this.wsLogger.warn("DISCONNECT", String.format("Executor (%s) did not terminate in time", tmp0));
            }
        } catch (InterruptedException ex4) {
            InterruptedException e = ex4;
            Thread.currentThread().interrupt();
            var tmp1 = new Object[1];
            tmp1[0] = name;
            this.wsLogger.error("DISCONNECT", String.format("Interrupted while shutting down %s executor", tmp1), e);
        }
    }

    private String describeThrowable(Throwable throwable) {
        if (throwable == null) return "unknown";
        String message = throwable.getMessage();
        if (message != null) {
            if (message.isBlank()) {
            }
        } else {
        }
        return message;
    }

    private static int extractHandshakeStatus(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (!((current instanceof WebSocketHandshakeException))) break label_9;
            WebSocketHandshakeException handshake = ((WebSocketHandshakeException) current);
            try {
                if (handshake.getResponse() != null) return handshake.getResponse().statusCode();
            } catch (Exception ex7) {
                Exception ignored = ex7;
                return -1;
            }
            label_9:
            current = current.getCause();
        }
        return -1;
    }

    private static boolean isFatalHandshakeStatus(int statusCode) {
        if (statusCode != 401 && statusCode != 403 && statusCode != 404) {
            if (statusCode == 426) {
            }
        } else {
        }
        return false;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public boolean isConnecting() {
        return this.connecting;
    }

    public boolean isReconnectEnabled() {
        return this.reconnectEnabled;
    }

    public int getReconnectAttempts() {
        return this.reconnectAttempts.get();
    }

    public long getConnectionDuration() {
        if ((!this.connected) || (this.connectionStartTime == 0L)) {
            return 0L;
        }
        return (System.currentTimeMillis() - this.connectionStartTime);
    }

    public String getServerUrl() {
        return this.serverUrl;
    }

    public Path getLogFilePath() {
        return this.wsLogger.getCurrentLogFile();
    }

    static {
        LOGGER = LoggerFactory.getLogger(VieWebSocketClient.class);
    }


    public interface MessageHandler {
        void onMessage(String arg0);

        void onKeyExpired(String arg0);

        void onKeyExpiringSoon(String arg0, int arg1);

        void onKeyRevoked(String arg0, String arg1);

        void onKeyUnrevoked(String arg0);

        void onKeyExtended(String arg0, String arg1);

        void onHwidReset(String arg0);

        void onKeyDeleted(String arg0);

    }

    class StrictX509TrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            throw new CertificateException("Client certificates not trusted");
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (chain != null) {
                if (chain.length == 0) {
                }
                throw new CertificateException("No server certificate found");
                try {
                }
                Exception e = chain;
                int var4 = e.length;
                int var5 = 0;
                while (var5 < var4) {
                    X509Certificate cert = e[var5];
                    cert.checkValidity();
                    var5++;
                }
            } catch (Exception ex7) {
                e = ex7;
                throw new CertificateException(("Certificate validation failed: " + e.getMessage()));
            }
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }

    final class ClientListener implements WebSocket.Listener {
        private final AtomicBoolean disconnectHandled;
        private final StringBuilder textBuffer;

        private ClientListener(VieWebSocketClient arg0) {
            this.this$0 = arg0;
            this.disconnectHandled = new AtomicBoolean(false);
            this.textBuffer = new StringBuilder();
        }

        public void onOpen(WebSocket webSocket) {
            this.this$0.wsClient = webSocket;
            this.this$0.connected = true;
            this.this$0.connecting = false;
            this.this$0.reconnectAttempts.set(0);
            this.this$0.connectionStartTime = System.currentTimeMillis();
            this.this$0.wsLogger.connection("OPEN", this.this$0.serverUrl, "Connection established successfully");
            this.this$0.authenticate();
            this.this$0.startPing();
            webSocket.request(1L);
        }

        public CompletionStage onText(WebSocket webSocket, CharSequence data, boolean last) {
            String message = null;
            Object var5 = this.textBuffer;
            // synchronized (this.textBuffer)
            this.textBuffer.append(data);
            if (!(!last)) {
                message = this.textBuffer.toString();
                this.textBuffer.setLength(0);
            }
            break label_10;
            Object var6 = null;
            throw var6;
            label_10:
            if (message != null) {
                this.this$0.wsLogger.message("RECV", "raw", message);
                this.this$0.handleMessage(message);
            }
            webSocket.request(1L);
            return CompletableFuture.completedFuture(null);
        }

        public CompletionStage onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            webSocket.request(1L);
            return CompletableFuture.completedFuture(null);
        }

        public CompletionStage onClose(WebSocket webSocket, int statusCode, String reason) {
            this.this$0.handleDisconnect(statusCode, reason, !this.this$0.closingByClient, this.disconnectHandled);
            return CompletableFuture.completedFuture(null);
        }

        public void onError(WebSocket webSocket, Throwable error) {
            var tmp0 = new Object[1];
            tmp0[0] = error.getMessage();
            this.this$0.wsLogger.error("ERROR", String.format("WebSocket error: %s", tmp0), error);
            VieWebSocketClient.LOGGER.error("[WebSocket] Error: {}", error.getMessage(), error);
            this.this$0.handleDisconnect(-1, ("error: " + this.this$0.describeThrowable(error)), !this.this$0.closingByClient, this.disconnectHandled);
        }

    }
}
