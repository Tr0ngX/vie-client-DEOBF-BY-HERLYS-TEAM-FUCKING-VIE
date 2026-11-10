package me.drake.viecheats.modules.client;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import me.drake.viecheats.VieCheatsMod;

public class DiscordPresence {
    private static final String DEFAULT_CLIENT_ID = "1501618761810055364";
    private static final String CLIENT_ID;
    private static final long UPDATE_INTERVAL_MS = 5000L;
    private static final int MAX_PIPE_INDEX = 63;
    private static final String ACTIVITY_NAME = "Vie Client";
    private static final String ACTIVITY_DETAILS = "Join discord for more info";
    private static final String DISCORD_BUTTON_LABEL = "Discord";
    private static final String DISCORD_INVITE_URL = "https://discord.gg/98S2MXZmDN";
    private RandomAccessFile pipe;
    private boolean connected;
    private long lastUpdate;
    private long startTimestamp;

    public DiscordPresence() {
        this.connected = false;
        this.lastUpdate = 0L;
    }

    public boolean connect() {
        if (!(!this.connected)) return true;
        closePipe();
        this.startTimestamp = (System.currentTimeMillis() / 1000L);
        try {
            IOException lastPipeError = null;
            int i = 0;
            while (i <= 63) {
                try {
                    this.pipe = new RandomAccessFile(("\\\\.\\pipe\\discord-ipc-" + i), "rw");
                } catch (IOException ex8) {
                    IOException e = ex8;
                    lastPipeError = e;
                    i++;
                }
            }
            label_11:
            if (this.pipe == null) {
                if (lastPipeError != null) throw lastPipeError;
                throw new IOException("Discord IPC pipes unavailable");
            }
            var tmp0 = new Object[1];
            tmp0[0] = CLIENT_ID;
            String handshake = String.format("{\"v\":1,\"client_id\":\"%s\"}", tmp0);
            sendFrame(0, handshake);
            String response = readFrame();
            if (response == null) throw new IOException("No handshake response");
            if (!(response.contains("\"evt\":\"READY\""))) throw new IOException(("Handshake rejected: " + trimForLog(response)));
            this.connected = true;
            this.lastUpdate = 0L;
            VieCheatsMod.LOGGER.info("[DiscordRPC] Connected to Discord (client_id={})", CLIENT_ID);
            updatePresence("Playing Vie Client");
            return true;
        } catch (Exception ex27) {
            e = ex27;
            var tmp1 = new Object[3];
            tmp1[0] = CLIENT_ID;
            tmp1[1] = 63;
            tmp1[2] = e.getMessage();
            VieCheatsMod.LOGGER.warn("[DiscordRPC] Failed to connect (client_id={}, pipes 0-{}): {}", tmp1);
            closePipe();
            this.connected = false;
            return false;
        }
    }

    public void disconnect() {
        if (!this.connected && this.pipe == null) return;
        if (!(!this.connected)) {
            clearActivity();
        }
        this.connected = false;
        closePipe();
        VieCheatsMod.LOGGER.info("[DiscordRPC] Disconnected");
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void tick(String detail) {
        if (!this.connected) return;
        long now = System.currentTimeMillis();
        if ((now - this.lastUpdate) < 5000L) return;
        this.lastUpdate = now;
        updatePresence(detail);
    }

    private void updatePresence(String detail) {
        if (this.pipe != null) break label_1;
        return;
        try {
            label_1:
            var tmp0 = new Object[8];
            tmp0[0] = ProcessHandle.current().pid();
            tmp0[1] = escapeJson("Vie Client");
            tmp0[2] = escapeJson("Join discord for more info");
            tmp0[3] = formatDisplayVersion("1.0.0");
            tmp0[4] = this.startTimestamp;
            tmp0[5] = escapeJson("Discord");
            tmp0[6] = escapeJson("https://discord.gg/98S2MXZmDN");
            tmp0[7] = System.nanoTime();
            String json = String.format("{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":%d,\"activity\":{\"name\":\"%s\",\"type\":0,\"details\":\"%s\",\"state\":\"v%s\",\"timestamps\":{\"start\":%d},\"assets\":{\"large_image\":\"vie_cheats\",\"large_text\":\"Vie Cheats\"},\"buttons\":[{\"label\":\"%s\",\"url\":\"%s\"}],\"instance\":true}},\"nonce\":\"%d\"}", tmp0);
            sendFrame(1, json);
        } catch (Exception ex13) {
            Exception e = ex13;
            VieCheatsMod.LOGGER.warn(("[DiscordRPC] Update failed: " + e.getMessage()));
            this.connected = false;
            closePipe();
        }
    }

    private void clearActivity() {
        if (this.pipe == null) {
            return;
            try {
            }
            var tmp0 = new Object[2];
            tmp0[0] = ProcessHandle.current().pid();
            tmp0[1] = UUID.randomUUID();
            String json = String.format("{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":%d,\"activity\":null},\"nonce\":\"%s\"}", tmp0);
            sendFrame(1, json);
        } catch (Exception ex7) {
            json = ex7;
        }
    }

    private void sendFrame(int opcode, String payload) throws IOException {
        if (this.pipe == null) throw new IOException("Discord IPC pipe is not opened.");
        byte[] data = payload.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate((8 + data.length));
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(opcode);
        buf.putInt(data.length);
        buf.put(data);
        this.pipe.write(buf.array());
    }

    private String readFrame() {
        try {
            byte[] header = new byte[8];
            this.pipe.readFully(header);
            ByteBuffer buf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
            buf.getInt();
            int length = buf.getInt();
            if ((length < 0) || (length > 1048576)) {
                throw new IOException(("Invalid Discord IPC frame length: " + length));
            }
            byte[] payload = new byte[length];
            this.pipe.readFully(payload);
            return new String(payload, StandardCharsets.UTF_8);
        } catch (Exception ex11) {
            Exception e = ex11;
            return null;
        }
    }

    private void closePipe() {
        try {
            if (this.pipe != null) {
                this.pipe.close();
            }
        } catch (IOException ex2) {
            Object var1 = ex2;
        }
        this.pipe = null;
    }

    private static String resolveClientId() {
        String fromProperty = System.getProperty("vie.discord.clientId", "").trim();
        if (!(fromProperty.isEmpty())) return fromProperty;
        String fromEnv = System.getenv("VIE_DISCORD_CLIENT_ID");
        if (fromEnv != null && !(fromEnv.isBlank())) return fromEnv.trim();
        return "1501618761810055364";
    }

    private static String formatDisplayVersion(String rawVersion) {
        if ((rawVersion == null) || (rawVersion.isBlank())) {
            return "1.0";
        }
        String[] parts = rawVersion.trim().split("\\.");
        if (parts.length >= 2) return (parts[0] + "." + parts[1]);
        return rawVersion.trim();
    }

    private String escapeJson(String s) {
        if (s == null) break label_1;
        if (s.isEmpty()) {
            label_1:
            return "";
        }
        StringBuilder out = new StringBuilder((s.length() + 16));
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            switch (c) {
                case 8 -> { break label_9; }
                case 9 -> { break label_13; }
                case 10 -> { break label_11; }
                case 12 -> { break label_10; }
                case 13 -> { break label_12; }
                case 34 -> { break label_8; }
                case 92 -> { break label_7; }
                default -> { break label_14; }
            }
            label_7:
            out.append("\\\\");
            break label_17;
            label_8:
            out.append("\\\"");
            break label_17;
            label_9:
            out.append("\\b");
            break label_17;
            label_10:
            out.append("\\f");
            break label_17;
            label_11:
            out.append("\\n");
            break label_17;
            label_12:
            out.append("\\r");
            break label_17;
            label_13:
            out.append("\\t");
            break label_17;
            label_14:
            if (c < 32) {
                var tmp0 = new Object[1];
                tmp0[0] = c;
                out.append(String.format("\\u%04x", tmp0));
                break label_17;
            }
            out.append(c);
            label_17:
            i++;
        }
        return out.toString();
    }

    private String trimForLog(String raw) {
        if ((raw == null) || (raw.isEmpty())) {
            return "";
        }
        if (raw.length() <= 180) return raw;
        return (raw.substring(0, 180) + "...");
    }

    static {
        CLIENT_ID = resolveClientId();
    }

}
