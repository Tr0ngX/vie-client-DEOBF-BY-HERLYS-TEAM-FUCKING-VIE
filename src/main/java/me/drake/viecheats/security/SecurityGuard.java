package me.drake.viecheats.security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import me.drake.viecheats.VieCheatsMod;
import net.minecraft.class_2561;
import net.minecraft.class_310;

public class SecurityGuard {
    private static final boolean ENABLED = true;
    private static final boolean STRICT_MODE = true;
    private static final Set TRUSTED_CLASSES;
    private static String jarChecksumSHA256;
    private static String jarChecksumMD5;
    private static String jarChecksumCRC32;
    private static boolean initialized;
    private static boolean compromised;
    private static long initialJarSize;
    private static String authStateHash;
    private static Object authStateLock;

    public static synchronized void initialize() {
        if (!initialized) {
            break label_2;
        }
        return;
        label_2:
        VieCheatsMod.LOGGER.info("[SecurityGuard] Initializing security checks...");
        try {
            checkDebugFlags();
            checkDebuggerAttached();
            checkEnvironment();
            checkAntiHooking();
            Thread checksumThread = new Thread(() -> {
    try {
        calculateMultipleChecksums();
        verifyJarSize();
    } catch (SecurityGuard.SecurityViolationException ex3) {
        SecurityGuard.SecurityViolationException e = ex3;
        compromised = true;
        VieCheatsMod.LOGGER.error("[SecurityGuard] Security violation: {}", e.getMessage());
        crash(("Security violation detected: " + e.getMessage()));
    } catch (Exception ex8) {
        e = ex8;
        compromised = true;
        VieCheatsMod.LOGGER.error("[SecurityGuard] Security check failed: {}", e.getMessage(), e);
        crash("Security check failed");
    }
    }
    label_12:
}, "vie-security-checksum");
            checksumThread.setDaemon(true);
            checksumThread.start();
            initialized = true;
            VieCheatsMod.LOGGER.info("[SecurityGuard] Security checks started (async checksum)");
        } catch (SecurityGuard.SecurityViolationException ex13) {
            SecurityGuard.SecurityViolationException e = ex13;
            compromised = true;
            VieCheatsMod.LOGGER.error("[SecurityGuard] Security violation: {}", e.getMessage());
            crash(("Security violation detected: " + e.getMessage()));
        } catch (Exception ex18) {
            e = ex18;
            compromised = true;
            VieCheatsMod.LOGGER.error("[SecurityGuard] Security check failed: {}", e.getMessage(), e);
            crash("Security check failed");
        }
        }
        label_22:
    }

    public static synchronized void verifyIntegrity() {
        if (!(!compromised)) {
            return;
            try {
            }
            checkDebugFlags();
            checkDebuggerAttached();
        } catch (SecurityGuard.SecurityViolationException ex5) {
            SecurityGuard.SecurityViolationException e = ex5;
            compromised = true;
            VieCheatsMod.LOGGER.error("[SecurityGuard] Runtime violation: {}", e.getMessage());
            crash(("Security violation: " + e.getMessage()));
        }
    }

    private static void verifyAuthStateIntegrity() throws SecurityGuard.SecurityViolationException {
        Object var0 = authStateLock;
        // synchronized (authStateLock)
        try {
            Class authManagerClass = Class.forName("me.drake.viecheats.auth.AuthManager");
            Object authManagerInstance = null;
            try {
                Class modClass = Class.forName("me.drake.viecheats.VieCheatsMod");
                Object modInstance = modClass.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
                if (modInstance != null) {
                    authManagerInstance = modClass.getMethod("getAuthManager", new Class[0]).invoke(modInstance, new Object[0]);
                }
            } catch (Exception ex8) {
                modClass = ex8;
            }
            if (authManagerInstance == null) break label_15;
            String currentHash = calculateAuthStateHash(authManagerInstance);
            if (!(authStateHash.isEmpty())) {
                if (currentHash.equals(authStateHash)) break label_15;
                throw new SecurityGuard.SecurityViolationException("Auth state tampered in memory");
            }
            authStateHash = currentHash;
            label_15:
        } catch (ClassNotFoundException ex16) {
            authManagerClass = ex16;
        } catch (Exception ex18) {
            Exception e = ex18;
            VieCheatsMod.LOGGER.warn("[SecurityGuard] Auth state integrity check failed: {}", e.getMessage());
        }
        }
        label_20:
        break label_24;
        Object var5 = null;
        throw var5;
        label_24:
    }

    private static String calculateAuthStateHash(Object authManager) throws Exception {
        try {
            Class authClass = authManager.getClass();
            Field statusField = authClass.getDeclaredField("status");
            statusField.setAccessible(true);
            Object status = statusField.get(authManager);
            Field keyCodeField = authClass.getDeclaredField("keyCode");
            keyCodeField.setAccessible(true);
            String keyCode = ((String) keyCodeField.get(authManager));
            Field lockedDownField = authClass.getDeclaredField("lockedDown");
            lockedDownField.setAccessible(true);
            boolean lockedDown = ((Boolean) lockedDownField.get(authManager)).booleanValue();
            String stateString = (status.toString() + keyCode + lockedDown);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(stateString.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            Object var12 = hash;
            int var13 = var12.length;
            int var14 = 0;
            while (var14 < var13) {
                byte b = var12[var14];
                var tmp0 = new Object[1];
                tmp0[0] = b;
                sb.append(String.format("%02x", tmp0));
                var14++;
            }
            return sb.toString();
        } catch (Exception ex20) {
            Exception e = ex20;
            throw new SecurityGuard.SecurityViolationException(("Failed to calculate auth state hash: " + e.getMessage()));
        }
    }

    private static void verifyRuntimeChecksums() throws SecurityGuard.SecurityViolationException {
        if ((jarChecksumSHA256.isEmpty()) || (jarChecksumMD5.isEmpty())) break label_1;
        if (!(jarChecksumCRC32.isEmpty())) break label_2;
        label_1:
        return;
        try {
            label_2:
            Path jarPath = resolveJarPath();
            if (jarPath != null) {
                byte[] jarBytes = Files.readAllBytes(jarPath);
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] sha256Hash = sha256.digest(jarBytes);
                StringBuilder sb = new StringBuilder();
                String currentSHA256 = sha256Hash;
                MessageDigest md5 = currentSHA256.length;
                byte[] md5Hash = 0;
                while (md5Hash < md5) {
                    byte b = currentSHA256[md5Hash];
                    var tmp0 = new Object[1];
                    tmp0[0] = b;
                    sb.append(String.format("%02x", tmp0));
                    md5Hash++;
                }
                currentSHA256 = sb.toString();
                if (!(currentSHA256.equals(jarChecksumSHA256))) throw new SecurityGuard.SecurityViolationException("SHA-256 checksum mismatch");
                md5 = MessageDigest.getInstance("MD5");
                md5Hash = md5.digest(jarBytes);
                sb = new StringBuilder();
                b = md5Hash;
                CRC32 crc32 = b.length;
                String currentCRC32 = 0;
                while (currentCRC32 < crc32) {
                    b = b[currentCRC32];
                    var tmp1 = new Object[1];
                    tmp1[0] = b;
                    sb.append(String.format("%02x", tmp1));
                    currentCRC32++;
                }
                String currentMD5 = sb.toString();
                if (!(currentMD5.equals(jarChecksumMD5))) throw new SecurityGuard.SecurityViolationException("MD5 checksum mismatch");
                crc32 = new CRC32();
                crc32.update(jarBytes);
                currentCRC32 = Long.toHexString(crc32.getValue());
                if (!(currentCRC32.equals(jarChecksumCRC32))) throw new SecurityGuard.SecurityViolationException("CRC32 checksum mismatch");
            }
        } catch (SecurityGuard.SecurityViolationException ex31) {
            SecurityGuard.SecurityViolationException e = ex31;
            throw e;
        } catch (Exception ex33) {
            e = ex33;
            VieCheatsMod.LOGGER.warn("[SecurityGuard] Runtime checksum verification failed: {}", e.getMessage());
        }
    }

    private static void checkClassIntegrity() throws SecurityGuard.SecurityViolationException {
        Object var0 = TRUSTED_CLASSES.iterator();
        while (var0.hasNext()) {
            String className = ((String) var0.next());
            try {
                Class clazz = Class.forName(className);
                byte[] bytecode = getClassBytes(clazz);
                if ((bytecode == null) || (bytecode.length == 0)) {
                    throw new SecurityGuard.SecurityViolationException(("Class tampered: " + className));
                }
                if (containsSuspiciousPatterns(bytecode)) throw new SecurityGuard.SecurityViolationException(("Suspicious bytecode: " + className));
                if (!(isValidClassStructure(bytecode))) throw new SecurityGuard.SecurityViolationException(("Invalid class structure: " + className));
            } catch (ClassNotFoundException ex11) {
                ClassNotFoundException e = ex11;
                throw new SecurityGuard.SecurityViolationException(("Missing trusted class: " + className));
            }
        }
    }

    private static boolean isValidClassStructure(byte[] bytecode) {
        if ((bytecode == null) || (bytecode.length < 10)) {
            return false;
        }
        if ((((bytecode[0] != -54) || (bytecode[1] != -2)) || (bytecode[2] != -70)) || (bytecode[3] != -66)) {
            return false;
        }
        return true;
    }

    private static void checkDebugFlags() throws SecurityGuard.SecurityViolationException {
        boolean debugMode = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");
        if (!(!debugMode)) throw new SecurityGuard.SecurityViolationException("Debug mode detected");
    }

    private static void checkDebuggerAttached() throws SecurityGuard.SecurityViolationException {
        if (!(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-Xdebug"))) {
            if (ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-Xrunjdwp")) {
            }
        } else {
        }
        boolean debuggerAttached = false;
        if (!(!debuggerAttached)) throw new SecurityGuard.SecurityViolationException("Debugger attached");
    }

    private static void checkTampering() throws SecurityGuard.SecurityViolationException {
    }

    private static void checkEnvironment() throws SecurityGuard.SecurityViolationException {
        String vendor = System.getProperty("java.vm.vendor", "").toLowerCase();
        String name = System.getProperty("java.vm.name", "").toLowerCase();
        if (!(vendor.contains("android")) && !(name.contains("android"))) {
            if (vendor.contains("dalvik")) {
            }
        } else {
        }
        boolean isAndroid = false;
        if (!(!isAndroid)) {
            VieCheatsMod.LOGGER.info("[SecurityGuard] Android environment detected, skipping VM/sandbox checks");
            return;
        }
        String user = System.getProperty("user.name", "").toLowerCase();
        if (!(user.equals("vboxuser"))) {
            if (user.equals("vmware")) {
            }
            throw new SecurityGuard.SecurityViolationException("Sandbox environment detected");
            try {
            }
            if (isVirtualMachine()) throw new SecurityGuard.SecurityViolationException("Virtual machine detected");
        } catch (Exception ex15) {
            Object var4 = ex15;
        }
    }

    private static void calculateMultipleChecksums() {
        try {
            Path jarPath = resolveJarPath();
            if (jarPath != null) {
                byte[] jarBytes = Files.readAllBytes(jarPath);
                initialJarSize = ((long) jarBytes.length);
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] sha256Hash = sha256.digest(jarBytes);
                StringBuilder sb = new StringBuilder();
                MessageDigest md5 = sha256Hash;
                byte[] md5Hash = md5.length;
                CRC32 crc32 = 0;
                while (crc32 < md5Hash) {
                    byte b = md5[crc32];
                    var tmp0 = new Object[1];
                    tmp0[0] = b;
                    sb.append(String.format("%02x", tmp0));
                    crc32++;
                }
                jarChecksumSHA256 = sb.toString();
                md5 = MessageDigest.getInstance("MD5");
                md5Hash = md5.digest(jarBytes);
                sb = new StringBuilder();
                crc32 = md5Hash;
                b = crc32.length;
                int var9 = 0;
                while (var9 < b) {
                    b = crc32[var9];
                    var tmp1 = new Object[1];
                    tmp1[0] = b;
                    sb.append(String.format("%02x", tmp1));
                    var9++;
                }
                jarChecksumMD5 = sb.toString();
                crc32 = new CRC32();
                crc32.update(jarBytes);
                jarChecksumCRC32 = Long.toHexString(crc32.getValue());
            }
        } catch (Exception ex24) {
            Exception e = ex24;
            VieCheatsMod.LOGGER.warn("[SecurityGuard] Could not calculate JAR checksums: {}", e.getMessage());
        }
    }

    private static void verifyJarSize() throws SecurityGuard.SecurityViolationException {
        if (initialJarSize == 0L) {
            return;
            try {
            }
            Path jarPath = resolveJarPath();
            if (jarPath != null) {
                long currentSize = Files.size(jarPath);
                if (currentSize != initialJarSize) throw new SecurityGuard.SecurityViolationException(("JAR size changed: expected " + initialJarSize + ", got " + currentSize));
            }
        } catch (IOException ex7) {
            IOException e = ex7;
            VieCheatsMod.LOGGER.warn("[SecurityGuard] Could not verify JAR size");
        }
    }

    private static void checkAntiHooking() throws SecurityGuard.SecurityViolationException {
        try {
            SecurityGuard.SecurityViolationException e = ManagementFactory.getRuntimeMXBean().getInputArguments().iterator();
            while (e.hasNext()) {
                String arg = ((String) e.next());
                if (arg.toLowerCase().contains("-javaagent")) {
                    String lower = arg.toLowerCase();
                    if (((((((((((((((lower.contains("fabric")) || (lower.contains("mixin"))) || (lower.contains("theseus"))) || (lower.contains("lunar"))) || (lower.contains("overwolf"))) || (lower.contains("curseforge"))) || (lower.contains("fabric-share"))) || (lower.contains("movtery"))) || (lower.contains("zalith"))) || (lower.contains("miolibpatcher"))) || (lower.contains("pojav"))) || (lower.contains("cacio"))) || (lower.contains("caciocavallo"))) || (lower.contains("authlib"))) || (lower.contains("nide8"))) break label_1;
                    if (lower.contains("forge")) {
                        break label_1;
                    }
                    throw new SecurityGuard.SecurityViolationException(("Java agent detected: " + arg));
                }
            }
        } catch (SecurityGuard.SecurityViolationException ex16) {
            e = ex16;
            throw e;
        } catch (Exception ex18) {
            e = ex18;
            try {
            }
            Instrumentation inst = ((Instrumentation) Instrumentation.class.cast(Class.forName("me.drake.viecheats.security.SecurityGuard").getClassLoader().loadClass("java.lang.instrument.Instrumentation").getMethod("getInstrumentation", new Class[0]).invoke(null, new Object[0])));
            if ((inst == null) || (inst.getAllLoadedClasses().length <= 0)) break label_37;
            arg = inst.getAllLoadedClasses();
            lower = arg.length;
            int var3 = 0;
            while (var3 < lower) {
                Class clazz = arg[var3];
                String name = clazz.getName().toLowerCase();
                if (!(name.contains("agent")) && !(name.contains("instrument"))) {
                    if (!(name.contains("transform"))) break label_36;
                }
                if (((((name.contains("movtery")) || (name.contains("zalith"))) || (name.contains("miolibpatcher"))) || (name.contains("cacio"))) || (name.contains("caciocavallo"))) break label_36;
                if (name.contains("pojav")) {
                    break label_36;
                }
                throw new SecurityGuard.SecurityViolationException("Instrumentation detected");
            }
            label_36:
            var3++;
        }
        label_37:
        } catch (Exception ex38) {
            e = ex38;
        }
    }

    private static byte[] getClassBytes(Class clazz) {
        try {
            String className = (clazz.getName().replace(46, 47) + ".class");
            InputStream is = clazz.getClassLoader().getResourceAsStream(className);
            if (is == null) {
                return null;
                try {
                }
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[4096];
                label_7:
                var tmp0 = is.read(data, 0, data.length);
                int nRead = tmp0;
                if (tmp0 != -1) {
                    buffer.write(data, 0, nRead);
                    break label_7;
                }
                return buffer.toByteArray();
            } catch (IOException ex12) {
                IOException e = ex12;
                return null;
            }
        }
    }

    private static boolean containsSuspiciousPatterns(byte[] bytecode) {
        if ((bytecode == null) || (bytecode.length < 10)) {
            return true;
        }
        String bytecodeStr = new String(bytecode);
        if ((bytecodeStr.contains("jdwp")) || (bytecodeStr.contains("debugger"))) {
            return true;
        }
        return false;
    }

    private static boolean isVirtualMachine() {
        try {
            var tmp0 = new String[8];
            tmp0[0] = "vmware";
            tmp0[1] = "virtualbox";
            tmp0[2] = "qemu";
            tmp0[3] = "xen";
            tmp0[4] = "kvm";
            tmp0[5] = "parallels";
            tmp0[6] = "hyperv";
            tmp0[7] = "bochs";
            String[] vmIndicators = tmp0;
            String cpuInfo = "";
            try {
                if (!(System.getProperty("os.name", "").toLowerCase().contains("linux"))) break label_6;
                Path cpuPath = Paths.get("/proc/cpuinfo", new String[0]);
                if (!(Files.exists(cpuPath, new LinkOption[0]))) break label_6;
                cpuInfo = Files.readString(cpuPath).toLowerCase();
                label_6:
            } catch (Exception ex7) {
                cpuPath = ex7;
            }
            cpuPath = vmIndicators;
            Path dmiPath = cpuPath.length;
            String productName = 0;
            label_9:
            if (productName < dmiPath) {
                String indicator = cpuPath[productName];
                if (cpuInfo.contains(indicator)) {
                    return true;
                    try {
                    }
                    productName++;
                    break label_9;
                    try {
                    }
                    String macAddress = getMacAddress();
                    if (!(macAddress.startsWith("00:05:69")) && !(macAddress.startsWith("00:0C:29")) && !(macAddress.startsWith("00:50:56")) && !(macAddress.startsWith("00:1C:14")) && !(macAddress.startsWith("00:16:3E")) && !(macAddress.startsWith("08:00:27")) && !(macAddress.startsWith("00:1A:4A"))) {
                        if (macAddress.startsWith("00:0F:4B")) {
                        }
                        return true;
                        try {
                        }
                    } catch (Exception ex26) {
                        cpuPath = ex26;
                    }
                    String os = System.getProperty("os.name", "").toLowerCase();
                    if (!(os.contains("linux"))) break label_48;
                    try {
                        dmiPath = Paths.get("/sys/class/dmi/id/product_name", new String[0]);
                        if (!(Files.exists(dmiPath, new LinkOption[0]))) break label_37;
                        productName = Files.readString(dmiPath).toLowerCase();
                        if (!(productName.contains("vmware")) && !(productName.contains("virtualbox")) && !(productName.contains("qemu")) && !(productName.contains("kvm")) && !(productName.contains("xen"))) {
                            if (!(productName.contains("parallels"))) break label_37;
                        }
                        return true;
                        try {
                            label_37:
                        } catch (Exception ex38) {
                            dmiPath = ex38;
                            try {
                            }
                            Path biosPath = Paths.get("/sys/class/dmi/id/bios_vendor", new String[0]);
                            if (!(Files.exists(biosPath, new LinkOption[0]))) break label_46;
                            String biosVendor = Files.readString(biosPath).toLowerCase();
                            if (!(biosVendor.contains("vmware")) && !(biosVendor.contains("virtualbox")) && !(biosVendor.contains("qemu"))) {
                                if (!(biosVendor.contains("bochs"))) break label_46;
                            }
                            return true;
                            try {
                                label_46:
                            } catch (Exception ex47) {
                                dmiPath = ex47;
                            }
                            label_48:
                            if (!(os.contains("windows"))) break label_60;
                            try {
                                var tmp1 = new String[4];
                                tmp1[0] = "wmic";
                                tmp1[1] = "computersystem";
                                tmp1[2] = "get";
                                tmp1[3] = "manufacturer";
                                Process process = new ProcessBuilder(tmp1).redirectErrorStream(true).start();
                                String output = new String(process.getInputStream().readAllBytes()).toLowerCase();
                                if (!(output.contains("vmware")) && !(output.contains("virtualbox")) && !(output.contains("qemu")) && !(output.contains("xen"))) {
                                    if (output.contains("microsoft corporation") && output.contains("virtual")) {
                                    }
                                    return true;
                                    try {
                                    }
                                } catch (Exception ex59) {
                                    dmiPath = ex59;
                                    try {
                                    }
                                    label_60:
                                    if (!(System.getProperty("os.name", "").toLowerCase().contains("linux"))) break label_67;
                                    Path cpuFlagsPath = Paths.get("/proc/cpuinfo", new String[0]);
                                    if (!(Files.exists(cpuFlagsPath, new LinkOption[0]))) break label_67;
                                    String cpuInfo2 = Files.readString(cpuFlagsPath).toLowerCase();
                                    if (!(cpuInfo2.contains("hypervisor"))) break label_67;
                                    return true;
                                    try {
                                        label_67:
                                    } catch (Exception ex68) {
                                        dmiPath = ex68;
                                    }
                                    if (detectVMTiming()) {
                                        return true;
                                        try {
                                        }
                                        return false;
                                    } catch (Exception ex74) {
                                        Exception e = ex74;
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean detectVMTiming() {
        try {
            long start = System.nanoTime();
            int i = 0;
            while (i < 10000) {
                Math.sqrt(((double) i));
                i++;
            }
            long duration = (System.nanoTime() - start);
            if (duration <= 10000000L) {
                if (duration < 1000L) {
                }
                return true;
                try {
                }
                return false;
            } catch (Exception ex11) {
                Exception e = ex11;
                return false;
            }
        }
    }

    private static String getMacAddress() {
        try {
            NetworkInterface network = NetworkInterface.getByName("eth0");
            if (network == null) break label_13;
            byte[] mac = network.getHardwareAddress();
            if (mac == null) break label_13;
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < mac.length) {
                var tmp0 = new Object[2];
                tmp0[0] = mac[i];
                tmp0[1] = (i < (mac.length - 1) ? ":" : "");
                sb.append(String.format("%02X%s", tmp0));
                i++;
            }
            return sb.toString();
            label_13:
        } catch (Exception ex14) {
            network = ex14;
        }
        return "";
    }

    private static String getJarPath() {
        Path p = resolveJarPath();
        if (p == null) {
        } else {
        }
        return p.toString();
    }

    public static void crash(String reason) {
        VieCheatsMod.LOGGER.error("[SecurityGuard] Security violation: {}", reason);
        VieCheatsMod.LOGGER.error("[SecurityGuard] CRASHING: {}", reason);
        try {
            class_310 mc = class_310.method_1551();
            if (mc != null && mc.field_1724 != null) {
                mc.field_1724.method_7353(class_2561.method_43470(("\u00a7c[Security] " + reason)), false);
            }
        } catch (Exception ex6) {
            mc = ex6;
        }
        Runtime.getRuntime().halt(1);
    }

    public static boolean isCompromised() {
        return compromised;
    }

    public static void blockReflectionAccess() {
    }

    public static boolean verifyJarSignature() {
        try {
            Path jarPath = resolveJarPath();
            if (jarPath == null) {
                return false;
                try {
                }
                File jarFile = jarPath.toFile();
                if (!(jarFile.exists())) {
                    return false;
                    try {
                    }
                    ZipFile zipFile = new ZipFile(jarFile);
                    long totalSize = 0L;
                    Enumeration e = zipFile.entries();
                    while (e.hasMoreElements()) {
                        ZipEntry entry = ((ZipEntry) e.nextElement());
                        if (!(entry.isDirectory())) {
                            totalSize = (totalSize + entry.getSize());
                        }
                    }
                    zipFile.close();
                    return totalSize > 0L;
                } catch (Exception ex20) {
                    e = ex20;
                    VieCheatsMod.LOGGER.error("[SecurityGuard] JAR signature check failed: {}", e.getMessage());
                    return false;
                }
            }
        }
    }

    private static Path resolveJarPath() {
        try {
            ProtectionDomain pd = SecurityGuard.class.getProtectionDomain();
            if ((pd == null) || (pd.getCodeSource() == null)) break label_2;
            if (pd.getCodeSource().getLocation() == null) {
                label_2:
                return null;
                try {
                }
                URI uri = pd.getCodeSource().getLocation().toURI();
                Path path = null;
                if (!(!"file".equalsIgnoreCase(uri.getScheme()))) {
                    path = Paths.get(uri).toAbsolutePath().normalize();
                    break label_17;
                }
                String raw = uri.toString();
                if (!(raw.startsWith("jar:"))) break label_17;
                int bang = raw.indexOf(33);
                if (bang > 4) {
                } else {
                }
                String inner = raw.substring(4);
                URI innerUri = URI.create(inner);
                if (!"file".equalsIgnoreCase(innerUri.getScheme())) break label_17;
                path = Paths.get(innerUri).toAbsolutePath().normalize();
                label_17:
                if (path == null) {
                    return null;
                    try {
                    }
                    String lower = path.toString().toLowerCase(Locale.ROOT);
                    if (!(lower.endsWith(".jar"))) {
                        return null;
                        try {
                        }
                        if (Files.exists(path, new LinkOption[0])) {
                            if (!(Files.isRegularFile(path, new LinkOption[0]))) {
                            }
                            return null;
                            try {
                            }
                            return path;
                        } catch (Exception ex27) {
                            Exception ignored = ex27;
                            return null;
                        }
                    }
                }
            }
        }
    }

    static {
        TRUSTED_CLASSES = new HashSet();
        jarChecksumSHA256 = "";
        jarChecksumMD5 = "";
        jarChecksumCRC32 = "";
        initialized = false;
        compromised = false;
        initialJarSize = 0L;
        authStateHash = "";
        authStateLock = new Object();
        TRUSTED_CLASSES.add("me.drake.viecheats.VieCheatsMod");
        TRUSTED_CLASSES.add("me.drake.viecheats.auth.AuthManager");
        TRUSTED_CLASSES.add("me.drake.viecheats.security.ChimeraEngine");
        TRUSTED_CLASSES.add("me.drake.viecheats.internal.VieWebSocketClient");
        TRUSTED_CLASSES.add("me.drake.viecheats.security.SecurityGuard");
        TRUSTED_CLASSES.add("me.drake.viecheats.security.RuntimeGuard");
    }


    class SecurityViolationException extends Exception {
        public SecurityViolationException(String message) {
            super(message);
        }

    }
}
