package me.drake.viecheats.internal;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketLogger {
    private static final Logger LOGGER;
    private static final DateTimeFormatter DATE_FORMAT;
    private static final DateTimeFormatter TIMESTAMP_FORMAT;
    private static final boolean WS_VERBOSE_LOGS;
    private static final boolean WS_FILE_LOGS;
    private final Path logDir;
    private Path currentLogFile;
    private String currentDate;

    public WebSocketLogger(Path minecraftDir) {
        this.logDir = minecraftDir.resolve("viecheats").resolve("logs").resolve("websocket");
        if (!(!WS_FILE_LOGS)) {
            ensureLogDirectory();
            rotateLogFile();
        }
    }

    private void ensureLogDirectory() {
        try {
            Files.createDirectories(this.logDir, new FileAttribute[0]);
        } catch (IOException ex2) {
            IOException e = ex2;
            LOGGER.error("[WebSocketLogger] Failed to create log directory: {}", e.getMessage());
        }
    }

    private void rotateLogFile() {
        if (!WS_FILE_LOGS) return;
        String today = LocalDateTime.now().format(DATE_FORMAT);
        if (today.equals(this.currentDate)) break label_7;
        this.currentDate = today;
        this.currentLogFile = this.logDir.resolve(("websocket-" + today + ".log"));
        if (!WS_VERBOSE_LOGS) break label_7;
        LOGGER.info("[WebSocket:ROTATE] Log file rotated: {}", this.currentLogFile.getFileName());
        label_7:
    }

    private void writeToFile(String level, String tag, String message, Throwable throwable) {
        if (!WS_FILE_LOGS) return;
        rotateLogFile();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String threadName = Thread.currentThread().getName();
        var tmp0 = new Object[5];
        tmp0[0] = timestamp;
        tmp0[1] = threadName;
        tmp0[2] = level;
        tmp0[3] = tag;
        tmp0[4] = message;
        String logLine = String.format("[%s] [%s] [%s] [%s] %s", tmp0);
        if (throwable != null) {
            logLine = (logLine + "\n" + getStackTrace(throwable));
        }
        logLine = (logLine + "\n");
        try {
            var tmp1 = new OpenOption[2];
            tmp1[0] = StandardOpenOption.CREATE;
            tmp1[1] = StandardOpenOption.APPEND;
            Files.writeString(this.currentLogFile, logLine, StandardCharsets.UTF_8, tmp1);
        } catch (IOException ex10) {
            IOException e = ex10;
            LOGGER.error("[WebSocketLogger] Failed to write to log file: {}", e.getMessage());
        }
    }

    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");
        Object var3 = throwable.getStackTrace();
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            StackTraceElement element = var3[var5];
            sb.append("\tat ").append(element.toString()).append("\n");
            var5++;
        }
        return sb.toString();
    }

    public void debug(String tag, String message) {
        if (!WS_VERBOSE_LOGS) return;
        LOGGER.debug("[WebSocket:{}] {}", tag, message);
        writeToFile("DEBUG", tag, message, null);
    }

    public void debug(String tag, String message, Object... args) {
        String formatted = String.format(message, args);
        debug(tag, formatted);
    }

    public void info(String tag, String message) {
        if (!WS_VERBOSE_LOGS) return;
        LOGGER.info("[WebSocket:{}] {}", tag, message);
        writeToFile("INFO", tag, message, null);
    }

    public void info(String tag, String message, Object... args) {
        String formatted = String.format(message, args);
        info(tag, formatted);
    }

    public void warn(String tag, String message) {
        LOGGER.warn("[WebSocket:{}] {}", tag, message);
        writeToFile("WARN", tag, message, null);
    }

    public void warn(String tag, String message, Object... args) {
        String formatted = String.format(message, args);
        warn(tag, formatted);
    }

    public void error(String tag, String message) {
        LOGGER.error("[WebSocket:{}] {}", tag, message);
        writeToFile("ERROR", tag, message, null);
    }

    public void error(String tag, String message, Throwable throwable) {
        var tmp0 = new Object[3];
        tmp0[0] = tag;
        tmp0[1] = message;
        tmp0[2] = throwable;
        LOGGER.error("[WebSocket:{}] {}", tmp0);
        writeToFile("ERROR", tag, message, throwable);
    }

    public void error(String tag, String message, Object... args) {
        String formatted = String.format(message, args);
        error(tag, formatted);
    }

    public void connection(String state, String url, String details) {
        String maskedUrl = maskUrl(url);
        var tmp0 = new Object[3];
        tmp0[0] = state;
        tmp0[1] = maskedUrl;
        tmp0[2] = details;
        String message = String.format("Connection %s: %s (%s)", tmp0);
        info("CONN", message);
    }

    private String maskUrl(String url) {
        if (url == null) {
            return "null";
            try {
            }
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            int port = uri.getPort();
            String path = uri.getPath();
            var tmp0 = new Object[3];
            tmp0[0] = scheme;
            tmp0[1] = port;
            tmp0[2] = path;
            return String.format("%s://***:%d%s", tmp0);
        } catch (Exception ex7) {
            Exception e = ex7;
            return "***";
        }
    }

    public void auth(String state, String keyCode, String details) {
        if (keyCode != null && keyCode.length() > 8) {
        } else {
        }
        String maskedKey = "null";
        var tmp0 = new Object[3];
        tmp0[0] = state;
        tmp0[1] = maskedKey;
        tmp0[2] = details;
        String message = String.format("Auth %s: key=%s, %s", tmp0);
        info("AUTH", message);
    }

    public void message(String direction, String type, String content) {
        if (content != null && content.length() > 200) {
        } else {
        }
        String truncated = content;
        var tmp0 = new Object[3];
        tmp0[0] = direction;
        tmp0[1] = type;
        tmp0[2] = truncated;
        String message = String.format("Message %s: type=%s, content=%s", tmp0);
        debug("MSG", message);
    }

    public void ssl(String state, String details) {
        var tmp0 = new Object[2];
        tmp0[0] = state;
        tmp0[1] = details;
        String message = String.format("SSL %s: %s", tmp0);
        info("SSL", message);
    }

    public void reconnectAttempt(int attemptNumber, String reason) {
        var tmp0 = new Object[2];
        tmp0[0] = attemptNumber;
        tmp0[1] = reason;
        String message = String.format("Reconnect attempt #%d: %s", tmp0);
        warn("RECONNECT", message);
    }

    public Path getLogDirectory() {
        return this.logDir;
    }

    public Path getCurrentLogFile() {
        if (!WS_FILE_LOGS) return null;
        rotateLogFile();
        return this.currentLogFile;
    }

    static {
        LOGGER = LoggerFactory.getLogger(WebSocketLogger.class);
        DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        WS_VERBOSE_LOGS = Boolean.parseBoolean(String.valueOf(System.getenv().getOrDefault("VIE_WS_VERBOSE_LOGS", "false")));
        WS_FILE_LOGS = Boolean.parseBoolean(String.valueOf(System.getenv().getOrDefault("VIE_WS_FILE_LOGS", "false")));
    }

}
