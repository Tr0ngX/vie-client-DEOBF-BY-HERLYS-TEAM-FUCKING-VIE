package me.drake.viecheats.security;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import me.drake.viecheats.VieCheatsMod;

public final class RuntimeGuard {
    private static final List DEBUG_FLAGS;
    private static final List FORBIDDEN_WINDOWS_PROCESSES;
    private static final long PROCESS_SCAN_INTERVAL_MS = 90000L;
    private static final long JAR_INTEGRITY_INTERVAL_MS = 120000L;
    private static final long BACKGROUND_TICK_MS = 5000L;
    private final String expectedJarSha256;
    private final ScheduledExecutorService backgroundScanner;
    private long nextJarVerifyAtMs;
    private long nextProcessScanAtMs;
    private volatile String compromiseReason;

    public RuntimeGuard() {
        this.nextJarVerifyAtMs = 0L;
        this.nextProcessScanAtMs = 0L;
        this.expectedJarSha256 = readStringProperty("VIE_CLIENT_SHA256").trim().toLowerCase(Locale.ROOT);
        this.backgroundScanner = Executors.newSingleThreadScheduledExecutor((r) -> {
    Thread t = new Thread(r, "vc-runtime-guard");
    t.setDaemon(true);
    t.setPriority(1);
    return t;
});
        this.backgroundScanner.scheduleWithFixedDelay(this::runBackgroundChecks, 5000L, 5000L, TimeUnit.MILLISECONDS);
    }

    public synchronized String checkStartup() {
        if (this.compromiseReason != null) return this.compromiseReason;
        String reason = detectDebugFlag();
        if (reason != null) return remember(reason);
        reason = verifyJarIntegrity();
        if (reason != null) return remember(reason);
        return null;
    }

    public synchronized String checkPeriodic() {
        if (this.compromiseReason != null) return this.compromiseReason;
        String reason = detectDebugFlag();
        if (reason != null) return remember(reason);
        return this.compromiseReason;
    }

    private void runBackgroundChecks() {
        if (this.compromiseReason == null) break label_1;
        return;
        try {
            label_1:
            long now = System.currentTimeMillis();
            if (now >= this.nextJarVerifyAtMs) {
                String reason = verifyJarIntegrity();
                if (reason != null) {
                    remember(reason);
                    return;
                    try {
                    }
                    this.nextJarVerifyAtMs = (now + 120000L);
                }
                if (now >= this.nextProcessScanAtMs) {
                    reason = scanForbiddenProcesses();
                    if (reason != null) {
                        remember(reason);
                        return;
                        try {
                        }
                        this.nextProcessScanAtMs = (now + 90000L);
                    }
                } catch (Throwable ex15) {
                    now = ex15;
                }
            }
        }
    }

    private String remember(String reason) {
        this.compromiseReason = reason;
        return reason;
    }

    private static String detectDebugFlag() {
        try {
            String vmInfo = ManagementFactory.getRuntimeMXBean().getInputArguments().iterator();
            while (vmInfo.hasNext()) {
                String arg = ((String) vmInfo.next());
                String lowered = arg.toLowerCase(Locale.ROOT);
                Object var3 = DEBUG_FLAGS.iterator();
                while (var3.hasNext()) {
                    String flag = ((String) var3.next());
                    if (lowered.contains(flag)) {
                        return ("Debug argument detected: " + arg);
                        try {
                        }
                    }
                }
            } catch (Exception ex11) {
                vmInfo = ex11;
            }
            vmInfo = System.getProperty("java.vm.info", "");
            if (vmInfo != null && vmInfo.toLowerCase(Locale.ROOT).contains("debug")) return "Debug VM info detected.";
            String allowAttachSelf = System.getProperty("jdk.attach.allowAttachSelf", "false");
            if (!(!"true".equalsIgnoreCase(allowAttachSelf))) return "Self-attach debugging is enabled (jdk.attach.allowAttachSelf=true).";
            return null;
        }
    }

    private String verifyJarIntegrity() {
        if (isAndroidRuntime()) return null;
        if (this.expectedJarSha256.isBlank()) {
            return null;
            try {
            }
            CodeSource codeSource = VieCheatsMod.class.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                if (codeSource.getLocation() == null) {
                }
                return "Cannot resolve code source for integrity check.";
                try {
                }
                URI uri = codeSource.getLocation().toURI();
                Path currentJar = Path.of(uri);
            } catch (Exception ex9) {
                Exception e = ex9;
                return ("Cannot resolve runtime jar path: " + e.getMessage());
            }
            if (!(Files.isRegularFile(currentJar, new LinkOption[0]))) {
                return ("Expected jar file for integrity check but got: " + String.valueOf(currentJar));
                try {
                }
                String actualHash = sha256File(currentJar);
            } catch (IOException ex15) {
                e = ex15;
                return ("Cannot hash runtime jar: " + e.getMessage());
            }
            if (!(this.expectedJarSha256.equalsIgnoreCase(actualHash))) return "Jar integrity mismatch.";
            return null;
        }
    }

    private static String scanForbiddenProcesses() {
        if (!(isWindows())) {
            return null;
            try {
            }
            boolean suspicious = ProcessHandle.allProcesses().anyMatch((ph) -> {
    ProcessHandle.Info info = ph.info();
    String command = ((String) info.command().orElse("")).toLowerCase(Locale.ROOT);
    String commandLine = ((String) info.commandLine().orElse("")).toLowerCase(Locale.ROOT);
    String snapshot = (command + " " + commandLine);
    Object var5 = FORBIDDEN_WINDOWS_PROCESSES.iterator();
    while (var5.hasNext()) {
        String processName = ((String) var5.next());
        if (snapshot.contains(processName.toLowerCase(Locale.ROOT))) return true;
    }
    return false;
});
            if (!(!suspicious)) return "Analysis process detected.";
        } catch (Throwable ex6) {
            suspicious = ex6;
        }
        return null;
    }

    private static String sha256File(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            InputStream in = Files.newInputStream(path, new OpenOption[0]);
            try {
                byte[] buffer = new byte[8192];
                label_3:
                var tmp0 = in.read(buffer);
                int read = tmp0;
                if (tmp0 >= 0) {
                    if (read == 0) {
                        break label_3;
                    }
                    digest.update(buffer, 0, read);
                    break label_3;
                }
                if (in == null) break label_13;
                in.close();
            } catch (Throwable ex8) {
                buffer = ex8;
                if (in == null) break label_12;
                try {
                    in.close();
                } catch (Throwable ex11) {
                    read = ex11;
                    buffer.addSuppressed(read);
                }
                label_12:
                throw buffer;
            }
            label_13:
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException ex15) {
            NoSuchAlgorithmException e = ex15;
            throw new IOException("SHA-256 is unavailable.", e);
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("windows");
    }

    private static boolean isAndroidRuntime() {
        String vendor = System.getProperty("java.vm.vendor", "").toLowerCase(Locale.ROOT);
        String vmName = System.getProperty("java.vm.name", "").toLowerCase(Locale.ROOT);
        if (!(vendor.contains("android")) && !(vmName.contains("dalvik"))) {
            if (vmName.contains("art")) {
            }
        } else {
        }
        return false;
    }

    private static boolean readBooleanProperty(String env, boolean fallback) {
        String value = readStringProperty(env);
        if (value.isBlank()) return fallback;
        Object var3 = value.trim().toLowerCase(Locale.ROOT);
        int var4 = -1;
        switch (var3.hashCode()) {
            case 48 -> { break label_7; }
            case 49 -> { break label_3; }
            case 3521 -> { break label_9; }
            case 3551 -> { break label_6; }
            case 109935 -> { break label_10; }
            case 119527 -> { break label_5; }
            case 3569038 -> { break label_4; }
            case 97196323 -> { break label_8; }
            default -> { break label_11; }
        }
        label_3:
        if (!(var3.equals("1"))) break label_11;
        var4 = 0;
        break label_11;
        label_4:
        if (!(var3.equals("true"))) break label_11;
        var4 = 1;
        break label_11;
        label_5:
        if (!(var3.equals("yes"))) break label_11;
        var4 = 2;
        break label_11;
        label_6:
        if (!(var3.equals("on"))) break label_11;
        var4 = 3;
        break label_11;
        label_7:
        if (!(var3.equals("0"))) break label_11;
        var4 = 4;
        break label_11;
        label_8:
        if (!(var3.equals("false"))) break label_11;
        var4 = 5;
        break label_11;
        label_9:
        if (!(var3.equals("no"))) break label_11;
        var4 = 6;
        break label_11;
        label_10:
        if (!(var3.equals("off"))) break label_11;
        var4 = 7;
        label_11:
        switch (var4) {
            case 0 -> { break label_12; }
            case 1 -> { break label_12; }
            case 2 -> { break label_12; }
            case 3 -> { break label_12; }
            case 4 -> { break label_13; }
            case 5 -> { break label_13; }
            case 6 -> { break label_13; }
            case 7 -> { break label_13; }
            default -> { break label_14; }
        }
        label_12:
        break label_15;
        label_13:
        break label_15;
        label_14:
        label_15:
        return fallback;
    }

    private static String readStringProperty(String env) {
        String fromEnv = System.getenv(env);
        if (fromEnv != null && !(fromEnv.isBlank())) return fromEnv;
        return "";
    }

    static {
        DEBUG_FLAGS = List.of("-agentlib:jdwp", "-xdebug", "-xrunjdwp");
        var tmp0 = new String[13];
        tmp0[0] = "x64dbg.exe";
        tmp0[1] = "x32dbg.exe";
        tmp0[2] = "ida64.exe";
        tmp0[3] = "ida.exe";
        tmp0[4] = "idaq64.exe";
        tmp0[5] = "ghidra.exe";
        tmp0[6] = "ollydbg.exe";
        tmp0[7] = "dnspy.exe";
        tmp0[8] = "processhacker.exe";
        tmp0[9] = "cheatengine.exe";
        tmp0[10] = "procmon.exe";
        tmp0[11] = "procexp.exe";
        tmp0[12] = "wireshark.exe";
        FORBIDDEN_WINDOWS_PROCESSES = List.of(tmp0);
    }

}
