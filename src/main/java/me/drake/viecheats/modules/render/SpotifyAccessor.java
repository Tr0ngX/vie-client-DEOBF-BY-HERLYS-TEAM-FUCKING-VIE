package me.drake.viecheats.modules.render;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.atomic.AtomicBoolean;
import me.drake.viecheats.VieCheatsMod;

public final class SpotifyAccessor {
    private static final boolean DEBUG = true;
    private static volatile SpotifyAccessor.WindowsMediaControlJNA nativeLib;
    private static volatile boolean initialized;
    private static volatile boolean nativeLoaded;
    private static volatile boolean connected;
    private static volatile boolean isPlaying;
    private static volatile int positionMs;
    private static volatile int durationMs;
    private static volatile String title;
    private static volatile String artist;
    private static volatile byte[] coverArtData;
    private static Thread pollThread;
    private static final AtomicBoolean running;
    private static volatile String cachedWindowTitle;
    private static final String DELIM = " - ";

    public static synchronized void initialize() {
        if (!(!initialized)) return;
        initialized = true;
        loadNativeLib();
        running.set(true);
        pollThread = new Thread(() -> {
    Thread.currentThread().setName("SpotifyAccessor-Poll");
    label_1:
    if (!(running.get())) break label_7;
    pollOnce();
    try {
        Thread.sleep(200L);
    } catch (InterruptedException ex5) {
        InterruptedException e = ex5;
        break label_7;
        label_7:
    }
}, "SpotifyAccessor-Poll");
        pollThread.setDaemon(true);
        pollThread.start();
        log("Background thread started");
    }

    private static void loadNativeLib() {
        try {
            var tmp0 = new String[1];
            tmp0[0] = "vie-spotify-natives";
            Path nativesDir = Path.of(System.getProperty("java.io.tmpdir"), tmp0);
            if (Files.exists(nativesDir, new LinkOption[0])) break label_3;
            Files.createDirectories(nativesDir, new FileAttribute[0]);
            label_3:
            var tmp1 = System.getProperty("os.arch").contains("64");
            boolean is64bit = tmp1;
            var tmp2 = (!(!tmp1) ? "windowsmediacontrol.dll" : "windowsmediacontrol_x86.dll");
            String dllName = tmp2;
            var tmp3 = nativesDir.resolve(tmp2);
            Path dllPath = tmp3;
            if (Files.exists(tmp3, new LinkOption[0])) break label_11;
            var tmp4 = new String[2];
            tmp4[0] = "spotify-api-natives";
            tmp4[1] = dllName;
            var tmp5 = Path.of(System.getProperty("java.io.tmpdir"), tmp4);
            Path alt = tmp5;
            if (!(Files.exists(tmp5, new LinkOption[0]))) break label_11;
            dllPath = alt;
            label_11:
            if (Files.exists(dllPath, new LinkOption[0])) break label_40;
            String archFolder = ("windows-x" + (!(!is64bit) ? "64" : "86"));
            var tmp6 = new String[2];
            tmp6[0] = ("/natives/" + archFolder + "/windowsmediacontrol.dll");
            tmp6[1] = ("/vie_cheats/natives/" + archFolder + "/windowsmediacontrol.dll");
            String[] resourceCandidates = tmp6;
            String selectedResource = null;
            InputStream selectedStream = null;
            InputStream in = resourceCandidates;
            int var10 = in.length;
            int var11 = 0;
            while (var11 < var10) {
                String resourcePath = in[var11];
                in = SpotifyAccessor.class.getResourceAsStream(resourcePath);
                if (in != null) {
                    selectedResource = resourcePath;
                    selectedStream = in;
                    break label_26;
                }
                var11++;
            }
            label_26:
            if (selectedStream == null) {
                log(("Native DLL not in classpath. Tried: " + String.join(", ", resourceCandidates)));
                return;
                try {
                }
                in = selectedStream;
                try {
                    var tmp7 = new CopyOption[1];
                    tmp7[0] = StandardCopyOption.REPLACE_EXISTING;
                    Files.copy(in, dllPath, tmp7);
                    log(("Extracted native DLL from: " + selectedResource));
                    if (in == null) break label_40;
                    in.close();
                } catch (Throwable ex35) {
                    var10 = ex35;
                    if (in == null) break label_39;
                    try {
                        in.close();
                    } catch (Throwable ex38) {
                        var11 = ex38;
                        var10.addSuppressed(var11);
                    }
                    label_39:
                    throw var10;
                }
                label_40:
                nativeLib = ((SpotifyAccessor.WindowsMediaControlJNA) Native.load(dllPath.toString(), SpotifyAccessor.WindowsMediaControlJNA.class));
                nativeLoaded = true;
                connected = true;
                log(("Native lib loaded: " + String.valueOf(dllPath)));
            } catch (Throwable ex45) {
                Throwable t = ex45;
                log(("Native load failed: " + t.getClass().getSimpleName() + ": " + t.getMessage()));
            }
        }
    }

    private static void pollOnce() {
        if (nativeLib != null) break label_3;
        pollWindowTitleOnce();
        return;
        try {
            label_3:
            int playing = nativeLib.isPlaying();
            isPlaying = playing == 1;
            long pos = nativeLib.getPlaybackPosition();
            if (pos >= 0L) {
                positionMs = ((int) pos);
            }
            long dur = nativeLib.getTrackDuration();
            if (nativeLib.getTrackDuration() > 0L) {
                durationMs = ((int) dur);
            }
            var tmp0 = nativeLib.getTrackTitle();
            Pointer tPtr = tmp0;
            if (tmp0 == null) break label_19;
            String t = tPtr.getString(0L, "UTF-8");
            nativeLib.freeString(tPtr);
            if ((t == null) || (t.isBlank())) break label_19;
            title = t;
            label_19:
            var tmp1 = nativeLib.getArtistName();
            Pointer aPtr = tmp1;
            if (tmp1 == null) break label_25;
            String a = aPtr.getString(0L, "UTF-8");
            nativeLib.freeString(aPtr);
            if ((a == null) || (a.isBlank())) break label_25;
            artist = a;
            label_25:
            var tmp2 = new PointerByReference();
            PointerByReference bufRef = tmp2;
            var tmp3 = new NativeLongByReference();
            NativeLongByReference lenRef = tmp3;
            if (!(nativeLib.getCoverArt(tmp2, tmp3))) break label_32;
            var tmp4 = bufRef.getValue();
            Pointer buf = tmp4;
            if ((tmp4 == null) || (lenRef.getValue().intValue() <= 0)) break label_32;
            int len = Math.min(lenRef.getValue().intValue(), 10000000);
            coverArtData = buf.getByteArray(0L, len);
            nativeLib.freeCoverArt(buf);
            label_32:
        } catch (Throwable ex33) {
            t = ex33;
            log(("Poll error: " + t.getClass().getSimpleName()));
        }
        pollWindowTitleOnce();
    }

    private static void pollWindowTitleOnce() {
        if ((title.isBlank()) || (artist.isBlank())) break label_2;
        return;
        try {
            label_2:
            String ps = "Get-Process -Name Spotify -ErrorAction SilentlyContinue | Select-Object -First 1 | ForEach-Object { $_.MainWindowTitle }";
            var tmp0 = new String[4];
            tmp0[0] = "powershell";
            tmp0[1] = "-NoProfile";
            tmp0[2] = "-Command";
            tmp0[3] = ps;
            Process p = new ProcessBuilder(tmp0).start();
            p.getOutputStream().close();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            try {
                StringBuilder sb = new StringBuilder();
                label_7:
                var tmp1 = r.readLine();
                String line = tmp1;
                if (tmp1 != null) {
                    sb.append(line);
                    break label_7;
                }
                String raw = sb.toString().trim();
                if (raw.equals(cachedWindowTitle)) {
                    r.close();
                    return;
                    try {
                    }
                    cachedWindowTitle = raw;
                    SpotifyAccessor.ParsedTrack parsed = parseWindowTitle(raw);
                    if (parsed != null) {
                        if (title.isBlank()) {
                            title = parsed.title;
                        }
                        if (artist.isBlank()) {
                            artist = parsed.artist;
                        }
                        log(("Window: " + artist + " - " + title));
                    }
                    r.close();
                } catch (Throwable ex23) {
                    line = ex23;
                    try {
                        r.close();
                    } catch (Throwable ex26) {
                        sb = ex26;
                        line.addSuppressed(sb);
                    }
                    throw line;
                }
            } catch (Throwable ex29) {
                ps = ex29;
            }
        }
    }

    private static SpotifyAccessor.ParsedTrack parseWindowTitle(String raw) {
        if ((raw == null) || (raw.isBlank())) {
            return null;
        }
        String t = raw.trim();
        if ((((t.equalsIgnoreCase("Spotify")) || (t.equalsIgnoreCase("Spotify Free"))) || (t.equalsIgnoreCase("Spotify Premium"))) || (t.isBlank())) {
            return null;
        }
        int firstDelim = t.indexOf(" - ");
        if (firstDelim <= 0) return new SpotifyAccessor.ParsedTrack(t, "");
        String a = t.substring(0, firstDelim).trim();
        String rest = t.substring((firstDelim + " - ".length())).trim();
        return new SpotifyAccessor.ParsedTrack(rest, a);
    }

    public static void poll() {
    }

    public static boolean isConnected() {
        return connected;
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static int getPositionMs() {
        return positionMs;
    }

    public static int getDurationMs() {
        return durationMs;
    }

    public static String getTitle() {
        return title;
    }

    public static String getArtist() {
        return artist;
    }

    public static byte[] getCoverArt() {
        return coverArtData;
    }

    public static synchronized void shutdown() {
        running.set(false);
        if (pollThread != null) {
            pollThread.interrupt();
            pollThread = null;
        }
        initialized = false;
    }

    private static void log(String msg) {
        VieCheatsMod.LOGGER.info(("[SpotifyAccessor] " + msg));
    }

    static {
        running = new AtomicBoolean(false);
        initialized = false;
        nativeLoaded = false;
        connected = false;
        isPlaying = false;
        positionMs = 0;
        durationMs = 180000;
        title = "";
        artist = "";
        coverArtData = null;
        cachedWindowTitle = "";
    }


    public interface WindowsMediaControlJNA extends Library {
        boolean isSpotifyAvailable();

        long getPlaybackPosition();

        long getTrackDuration();

        Pointer getTrackTitle();

        Pointer getArtistName();

        int isPlaying();

        boolean getCoverArt(PointerByReference arg0, NativeLongByReference arg1);

        void freeString(Pointer arg0);

        void freeCoverArt(Pointer arg0);

    }

    class ParsedTrack {
        final String title;
        final String artist;

        ParsedTrack(String title, String artist) {
            this.title = title;
            this.artist = artist;
        }

    }
}
