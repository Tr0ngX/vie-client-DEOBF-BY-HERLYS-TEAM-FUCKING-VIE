package me.drake.viecheats.modules.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.StringSetting;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5250;

public class FakeStats extends Module {
    private static FakeStats INSTANCE;
    private final BoolSetting sidebar;
    private final StringSetting title;
    private final StringSetting money;
    private final StringSetting shards;
    private final StringSetting kills;
    private final StringSetting deaths;
    private final StringSetting keyall;
    private final StringSetting playtime;
    private final StringSetting team;

    public FakeStats() {
        super("Fake Stats", "Client-side Donut scoreboard spoof (visual only)", Category.CLIENT);
        this.sidebar = ((BoolSetting) addSetting(new BoolSetting("Sidebar", "Rewrite scoreboard sidebar", true)));
        this.title = ((StringSetting) addSetting(new StringSetting("Title", "Fake sidebar title", "VIE CLIENT ON TOP")));
        this.money = ((StringSetting) addSetting(new StringSetting("Money", "Fake money value", "Vie Team")));
        this.shards = ((StringSetting) addSetting(new StringSetting("Shards", "Fake shards value", "Pedro sucked")));
        this.kills = ((StringSetting) addSetting(new StringSetting("Kills", "Fake kills value (blank = original)", "3618")));
        this.deaths = ((StringSetting) addSetting(new StringSetting("Deaths", "Fake deaths value", "Vie Client")));
        this.keyall = ((StringSetting) addSetting(new StringSetting("Keyall", "Fake keyall value", "Drake On Top")));
        this.playtime = ((StringSetting) addSetting(new StringSetting("Playtime", "Fake playtime value", "Donut sucks")));
        this.team = ((StringSetting) addSetting(new StringSetting("Team", "Fake team value", "Vie No1")));
        INSTANCE = this;
    }

    public String getInfo() {
        return null;
    }

    public static boolean isSurfaceActive(FakeStats.Surface surface) {
        FakeStats mod = INSTANCE;
        if ((mod == null) || (!(mod.isEnabled()))) break label_2;
        return mod.isSurfaceEnabled(surface);
    }

    public static class_2561 rewriteText(class_2561 original, FakeStats.Surface surface) {
        if (original == null) return null;
        FakeStats mod = INSTANCE;
        if (((mod == null) || (!(mod.isEnabled()))) || (!(mod.isSurfaceEnabled(surface)))) {
            return original;
        }
        String line = original.getString();
        if ((line == null) || (line.isEmpty())) {
            return original;
        }
        FakeStats.Replacement rep = mod.findReplacement(line, surface);
        if (rep == null) return original;
        class_2561 rebuilt = mod.rebuildWithStyle(original, rep);
        return (rebuilt == null ? original : rebuilt);
    }

    public static class_2561 rewriteSidebarTitle(class_2561 original) {
        if (original == null) return null;
        FakeStats mod = INSTANCE;
        if (((mod == null) || (!(mod.isEnabled()))) || (!(mod.isSurfaceEnabled(FakeStats.Surface.SIDEBAR)))) {
            return original;
        }
        String current = original.getString();
        if ((current == null) || (current.isEmpty())) {
            return original;
        }
        String replacement = safe(((String) mod.title.get()));
        if ((replacement.isEmpty()) || (replacement.equals(current))) {
            return original;
        }
        class_2561 rebuilt = mod.rebuildWithStyle(original, new FakeStats.Replacement(0, current.length(), replacement));
        if (rebuilt != null) return rebuilt;
        return class_2561.method_43470(replacement).method_10862(firstStyle(original));
    }

    public static class_2561 rewriteSidebarScoreForName(class_2561 sidebarName, class_2561 originalScore) {
        if (originalScore == null) return null;
        FakeStats mod = INSTANCE;
        if (((mod == null) || (!(mod.isEnabled()))) || (!(mod.isSurfaceEnabled(FakeStats.Surface.SIDEBAR)))) {
            return originalScore;
        }
        if (sidebarName == null) {
        } else {
        }
        String nameLine = sidebarName.getString();
        String label = mod.findStatLabel(nameLine);
        if (label == null) return originalScore;
        String currentScore = safe(originalScore.getString());
        if (currentScore.isEmpty()) return originalScore;
        String replacement = mod.valueForLabel(label);
        if (replacement == null) return originalScore;
        replacement = safe(replacement);
        if (replacement.isEmpty()) return originalScore;
        if (replacement.equals(currentScore)) return originalScore;
        return class_2561.method_43470(replacement).method_10862(firstStyle(originalScore));
    }

    private boolean isSurfaceEnabled(FakeStats.Surface surface) {
        if (surface != FakeStats.Surface.SIDEBAR) break label_1;
        return ((Boolean) this.sidebar.get()).booleanValue();
    }

    private String findStatLabel(String line) {
        if ((line == null) || (line.isEmpty())) {
            return null;
        }
        if (indexOfIgnoreCase(line, "Money") >= 0) return "Money";
        if (indexOfIgnoreCase(line, "Shards") >= 0) return "Shards";
        if (indexOfIgnoreCase(line, "Kills") >= 0) return "Kills";
        if (indexOfIgnoreCase(line, "Deaths") >= 0) return "Deaths";
        if (indexOfIgnoreCase(line, "Keyall") >= 0) return "Keyall";
        if (indexOfIgnoreCase(line, "Playtime") >= 0) return "Playtime";
        if (indexOfIgnoreCase(line, "Team") >= 0) return "Team";
        return null;
    }

    private String valueForLabel(String label) {
        Object var2 = label;
        int var3 = -1;
        switch (var2.hashCode()) {
            case -2050803966 -> { break label_5; }
            case -1819699083 -> { break label_2; }
            case 2602621 -> { break label_7; }
            case 72499381 -> { break label_3; }
            case 74526880 -> { break label_1; }
            case 1944356897 -> { break label_6; }
            case 2043062431 -> { break label_4; }
            default -> { break label_8; }
        }
        label_1:
        if (!(var2.equals("Money"))) break label_8;
        var3 = 0;
        break label_8;
        label_2:
        if (!(var2.equals("Shards"))) break label_8;
        var3 = 1;
        break label_8;
        label_3:
        if (!(var2.equals("Kills"))) break label_8;
        var3 = 2;
        break label_8;
        label_4:
        if (!(var2.equals("Deaths"))) break label_8;
        var3 = 3;
        break label_8;
        label_5:
        if (!(var2.equals("Keyall"))) break label_8;
        var3 = 4;
        break label_8;
        label_6:
        if (!(var2.equals("Playtime"))) break label_8;
        var3 = 5;
        break label_8;
        label_7:
        if (!(var2.equals("Team"))) break label_8;
        var3 = 6;
        label_8:
        switch (var3) {
            case 0 -> { break label_9; }
            case 1 -> { break label_10; }
            case 2 -> { break label_11; }
            case 3 -> { break label_12; }
            case 4 -> { break label_13; }
            case 5 -> { break label_14; }
            case 6 -> { break label_15; }
            default -> { break label_16; }
        }
        label_9:
        break label_17;
        label_10:
        break label_17;
        label_11:
        break label_17;
        label_12:
        break label_17;
        label_13:
        break label_17;
        label_14:
        break label_17;
        label_15:
        break label_17;
        label_16:
        label_17:
        return null;
    }

    private FakeStats.Replacement findReplacement(String line, FakeStats.Surface surface) {
        FakeStats.Replacement rep = findByLabel(line, "Money", safe(((String) this.money.get())));
        if (rep != null) return rep;
        rep = findByLabel(line, "Shards", safe(((String) this.shards.get())));
        if (rep != null) return rep;
        rep = findByLabel(line, "Kills", safe(((String) this.kills.get())));
        if (rep != null) return rep;
        rep = findByLabel(line, "Deaths", safe(((String) this.deaths.get())));
        if (rep != null) return rep;
        rep = findByLabel(line, "Keyall", safe(((String) this.keyall.get())));
        if (rep != null) return rep;
        rep = findByLabel(line, "Playtime", safe(((String) this.playtime.get())));
        if (rep != null) return rep;
        return findByLabel(line, "Team", safe(((String) this.team.get())));
    }

    private static String safe(String s) {
        if (s == null) {
        } else {
        }
        return s.trim();
    }

    private static FakeStats.Replacement findByLabel(String input, String label, String newValue) {
        if (((input == null) || (input.isEmpty())) || (label == null)) break label_1;
        if (label.isEmpty()) {
            label_1:
            return null;
        }
        if ((newValue == null) || (newValue.isEmpty())) {
            return null;
        }
        int idx = indexOfIgnoreCase(input, label);
        if (idx < 0) return null;
        int start = (idx + label.length());
        while (start < input.length() && isScoreSeparator(input.charAt(start))) {
            start++;
        }
        if (start > input.length()) {
            start = input.length();
        }
        int end = input.length();
        String oldValue = input.substring(start, end).trim();
        if (oldValue.isEmpty()) return null;
        if (oldValue.equals(newValue)) return null;
        return new FakeStats.Replacement(start, end, newValue);
    }

    private static boolean isScoreSeparator(char c) {
        if (!(Character.isWhitespace(c)) && c != 58 && c != 61 && c != 45 && c != 62 && c != 124) {
            if (c == 46) {
            }
        } else {
        }
        return false;
    }

    private static int indexOfIgnoreCase(String source, String target) {
        int max = (source.length() - target.length());
        int i = 0;
        while (i <= max) {
            if (source.regionMatches(true, i, target, 0, target.length())) return i;
            i++;
        }
        return -1;
    }

    private class_2561 rebuildWithStyle(class_2561 original, FakeStats.Replacement rep) {
        List segments = collectSegments(original);
        if (segments.isEmpty()) return null;
        int totalLen = ((FakeStats.StyledSegment) segments.get((segments.size() - 1))).end;
        int start = Math.max(0, Math.min(rep.start, totalLen));
        int end = Math.max(start, Math.min(rep.end, totalLen));
        class_2583 valueStyle = styleAt(segments, start, original.method_10866());
        class_5250 out = class_2561.method_43473();
        appendRange(out, segments, 0, start);
        if (!(rep.newValue.isEmpty())) {
            out.method_10852(class_2561.method_43470(rep.newValue).method_10862(valueStyle));
        }
        appendRange(out, segments, end, totalLen);
        return out;
    }

    private static List collectSegments(class_2561 text) {
        List out = new ArrayList();
        var tmp0 = new int[1];
        tmp0[0] = 0;
        int[] cursor = tmp0;
        text.method_27658((style, part) -> {
    if (part != null && !(part.isEmpty())) {
        int s = cursor[0];
        int e = (s + part.length());
        out.add(new FakeStats.StyledSegment(part, style, s, e));
        cursor[0] = e;
    }
    return Optional.empty();
}, class_2583.field_24360);
        return out;
    }

    private static class_2583 styleAt(List segments, int index, class_2583 fallback) {
        Object var3 = segments.iterator();
        while (var3.hasNext()) {
            FakeStats.StyledSegment seg = ((FakeStats.StyledSegment) var3.next());
            if (index >= seg.start && index < seg.end) return seg.style;
        }
        if (segments.isEmpty()) {
        } else {
        }
        return ((FakeStats.StyledSegment) segments.get((segments.size() - 1))).style;
    }

    private static class_2583 firstStyle(class_2561 text) {
        List segments = collectSegments(text);
        if (segments.isEmpty()) return text.method_10866();
        return ((FakeStats.StyledSegment) segments.get(0)).style;
    }

    private static void appendRange(class_5250 out, List segments, int from, int to) {
        if (from >= to) return;
        Object var4 = segments.iterator();
        while (var4.hasNext()) {
            FakeStats.StyledSegment seg = ((FakeStats.StyledSegment) var4.next());
            int s = Math.max(from, seg.start);
            int e = Math.min(to, seg.end);
            if (s >= e) {
                break label_2;
            }
            int localStart = (s - seg.start);
            int localEnd = (e - seg.start);
            String part = seg.text.substring(localStart, localEnd);
            if (!(part.isEmpty())) {
                out.method_10852(class_2561.method_43470(part).method_10862(seg.style));
            }
        }
    }


    record StyledSegment(String text, class_2583 style, int start, int end) {

        private StyledSegment(String text, class_2583 style, int start, int end) {
            this.text = text;
            this.style = style;
            this.start = start;
            this.end = end;
        }

        public String text() {
            return this.text;
        }

        public class_2583 style() {
            return this.style;
        }

        public int start() {
            return this.start;
        }

        public int end() {
            return this.end;
        }

    }

    record Replacement(int start, int end, String newValue) {

        private Replacement(int start, int end, String newValue) {
            this.start = start;
            this.end = end;
            this.newValue = newValue;
        }

        public int start() {
            return this.start;
        }

        public int end() {
            return this.end;
        }

        public String newValue() {
            return this.newValue;
        }

    }

    public enum Surface {
        SIDEBAR,
        TABLIST,
        CHAT;


        private Surface() {
        }

    }
}
