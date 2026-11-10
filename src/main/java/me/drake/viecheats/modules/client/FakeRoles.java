package me.drake.viecheats.modules.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.ColorSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.StringSetting;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import net.minecraft.class_5481;

public class FakeRoles extends Module {
    private static FakeRoles INSTANCE;
    private final BoolSetting enabled_setting;
    private final EnumSetting role;
    private final StringSetting customPrefix;
    private final ColorSetting roleColor;
    private final ColorSetting nameColor;
    private final BoolSetting chatReplace;
    private final BoolSetting hudReplace;

    public FakeRoles() {
        super("FakeRoles", "Replace your name with a fake role prefix in chat", Category.CLIENT);
        this.enabled_setting = ((BoolSetting) addSetting(new BoolSetting("Enabled", "Enable role replacement", true)));
        this.role = ((EnumSetting) addSetting(new EnumSetting("Role", "Fake role to display", FakeRoles.Role.OWNER, FakeRoles.Role.values())));
        this.customPrefix = ((StringSetting) addSetting(new StringSetting("CustomPrefix", "Custom role prefix text (overrides role)", "")));
        this.roleColor = ((ColorSetting) addSetting(new ColorSetting("RoleColor", "Color of the role prefix", -43691)));
        this.nameColor = ((ColorSetting) addSetting(new ColorSetting("NameColor", "Color of your name after role", -1)));
        this.chatReplace = ((BoolSetting) addSetting(new BoolSetting("ChatReplace", "Replace name in chat messages", true)));
        this.hudReplace = ((BoolSetting) addSetting(new BoolSetting("HUDReplace", "Replace name in HUD elements", true)));
        INSTANCE = this;
    }

    public String getInfo() {
        String prefix = getEffectivePrefix();
        return (prefix.isEmpty() ? "off" : prefix);
    }

    public static boolean isActive() {
        FakeRoles inst = INSTANCE;
        if ((inst == null) || (!(inst.isEnabled()))) break label_2;
        return ((Boolean) inst.enabled_setting.get()).booleanValue();
    }

    public static String getVisiblePlayerName() {
        class_310 mc = class_310.method_1551();
        if ((mc == null) || (mc.field_1724 == null)) {
            return null;
        }
        return mc.field_1724.method_5477().getString();
    }

    public static String getRealPlayerName() {
        return getVisiblePlayerName();
    }

    public static String getEffectivePrefix() {
        FakeRoles inst = INSTANCE;
        if (!(isActive())) return "";
        String custom = ((String) inst.customPrefix.get());
        if (custom != null && !(custom.isBlank())) {
            String trimmed = custom.trim();
            if (!(trimmed.startsWith("[")) && !(trimmed.endsWith("]"))) {
                trimmed = ("[" + trimmed + "]");
            }
            return trimmed;
        }
        return ((FakeRoles.Role) inst.role.get()).prefix();
    }

    public static class_2583 getRolePrefixStyle() {
        FakeRoles inst = INSTANCE;
        if (!(isActive())) return class_2583.field_24360;
        int color = ((Integer) inst.roleColor.get()).intValue();
        return class_2583.field_24360.method_36139(color);
    }

    public static class_2583 getRoleNameStyle() {
        FakeRoles inst = INSTANCE;
        if (!(isActive())) return class_2583.field_24360;
        int color = ((Integer) inst.nameColor.get()).intValue();
        return class_2583.field_24360.method_36139(color);
    }

    public static String getPrefixedNameString() {
        String prefix = getEffectivePrefix();
        String name = getVisiblePlayerName();
        if (((prefix.isEmpty()) || (name == null)) || (name.isBlank())) {
            return (name != null ? name : "");
        }
        return (prefix + " " + name);
    }

    public static String replaceInString(String input) {
        FakeRoles inst = INSTANCE;
        if ((!(isActive())) || (!(((Boolean) inst.chatReplace.get()).booleanValue()))) {
            return input;
        }
        if ((input == null) || (input.isBlank())) {
            return input;
        }
        String realName = getVisiblePlayerName();
        if ((realName == null) || (realName.isBlank())) {
            return input;
        }
        String prefixed = getPrefixedNameString();
        if (prefixed.equals(realName)) return input;
        return input.replace(realName, prefixed);
    }

    public static class_2561 replaceInText(class_2561 original) {
        FakeRoles inst = INSTANCE;
        if (!(isActive())) break label_2;
        if (((Boolean) inst.chatReplace.get()).booleanValue()) break label_3;
        label_2:
        return original;
        label_3:
        if (original == null) return null;
        String realName = getVisiblePlayerName();
        if ((realName == null) || (realName.isBlank())) {
            return original;
        }
        String prefixed = getPrefixedNameString();
        if (prefixed.equals(realName)) return original;
        String str = original.getString();
        if (!(str.contains(realName))) return original;
        List chunks = collectChunks(original);
        if (chunks.isEmpty()) return original;
        String rebuilt = rebuild(chunks);
        if (!(rebuilt.contains(realName))) return original;
        int pos = rebuilt.indexOf(realName);
        if (pos < 0) return original;
        class_5250 result = class_2561.method_43473();
        int cursor = 0;
        class_2583 suffixStyle = chunks.iterator();
        while (suffixStyle.hasNext()) {
            FakeRoles.StyledChunk chunk = ((FakeRoles.StyledChunk) suffixStyle.next());
            int chunkStart = chunk.start;
            int chunkEnd = chunk.end;
            if (cursor < chunkStart) {
                result.method_10852(class_2561.method_43470(rebuilt.substring(cursor, chunkStart)).method_10862(chunk.style));
                cursor = chunkStart;
            }
            if (cursor == pos) {
                class_2583 prefixStyle = getRolePrefixStyle();
                result.method_10852(class_2561.method_43470(getEffectivePrefix()).method_10862(prefixStyle));
                result.method_10852(class_2561.method_43470(" ").method_10862(prefixStyle));
                class_2583 nameStyle = getRoleNameStyle();
                result.method_10852(class_2561.method_43470(realName).method_10862(nameStyle));
                cursor = (pos + realName.length());
            }
        }
        if (cursor < rebuilt.length()) {
            suffixStyle = class_2583.field_24360;
            chunk = chunks.iterator();
            while (chunk.hasNext()) {
                chunk = ((FakeRoles.StyledChunk) chunk.next());
                if (cursor >= chunk.start && cursor < chunk.end) {
                    suffixStyle = chunk.style;
                    break label_42;
                }
            }
            label_42:
            result.method_10852(class_2561.method_43470(rebuilt.substring(cursor)).method_10862(suffixStyle));
        }
        return result;
    }

    public static class_5481 replaceOrderedText(class_5481 original) {
        FakeRoles inst = INSTANCE;
        if (!(isActive())) break label_2;
        if (((Boolean) inst.chatReplace.get()).booleanValue()) break label_3;
        label_2:
        return original;
        label_3:
        if (original == null) return original;
        String realName = getVisiblePlayerName();
        if (realName == null) break label_6;
        if (realName.isBlank()) {
            label_6:
            return original;
        }
        List chars = collectOrderedChars(original);
        StringBuilder sb = new StringBuilder();
        String str = chars.iterator();
        while (str.hasNext()) {
            FakeRoles.OrderedChar c = ((FakeRoles.OrderedChar) str.next());
            sb.append(c.chars);
        }
        str = sb.toString();
        if (!(str.contains(realName))) return original;
        int pos = str.indexOf(realName);
        if (pos < 0) return original;
        List positions = new ArrayList();
        int i = 0;
        while (i < chars.size()) {
            positions.add(((FakeRoles.OrderedChar) chars.get(i)).globalPos);
            i++;
        }
        List parts = new ArrayList();
        class_2583 prefixStyle = getRolePrefixStyle();
        class_2583 nameStyle = getRoleNameStyle();
        List prefixChars = new ArrayList();
        i = 0;
        while (i < getEffectivePrefix().length()) {
            prefixChars.add(String.valueOf(getEffectivePrefix().charAt(i)));
            i++;
        }
        List repParts = new ArrayList();
        i = prefixChars.iterator();
        while (i.hasNext()) {
            String pc = ((String) i.next());
            repParts.add(new FakeRoles.ReplacementPart(pc, prefixStyle));
        }
        repParts.add(new FakeRoles.ReplacementPart(" ", prefixStyle));
        i = pos;
        while (i < (pos + realName.length())) {
            repParts.add(new FakeRoles.ReplacementPart(String.valueOf(realName.charAt((i - pos))), nameStyle));
            i++;
        }
        int nameStartIdx = -1;
        int nameEndIdx = -1;
        i = 0;
        while (i < positions.size()) {
            if (((Integer) positions.get(i)).intValue() == pos) {
                nameStartIdx = i;
            }
            if (((Integer) positions.get(i)).intValue() == (pos + realName.length())) {
                nameEndIdx = i;
            }
            i++;
        }
        List resultParts = new ArrayList();
        if (nameStartIdx > 0) {
            List before = chars.subList(0, nameStartIdx);
            resultParts.add(concatOrdered(before));
        }
        before = repParts.iterator();
        while (before.hasNext()) {
            FakeRoles.ReplacementPart rp = ((FakeRoles.ReplacementPart) before.next());
            resultParts.add(class_5481.method_30747(rp.text, rp.style));
        }
        if (nameEndIdx >= 0 && nameEndIdx < chars.size()) {
            List after = chars.subList(nameEndIdx, chars.size());
            resultParts.add(concatOrdered(after));
            break label_62;
        }
        if ((nameEndIdx >= 0) || (nameStartIdx < 0)) break label_62;
        after = chars.subList((nameStartIdx + realName.length()), chars.size());
        resultParts.add(concatOrdered(after));
        label_62:
        if (resultParts.isEmpty()) return original;
        return class_5481.method_30749(resultParts);
    }

    private static class_5481 concatOrdered(List chars) {
        if (chars.isEmpty()) return class_5481.method_34905();
        List parts = new ArrayList();
        Object var2 = chars.iterator();
        while (var2.hasNext()) {
            FakeRoles.OrderedChar c = ((FakeRoles.OrderedChar) var2.next());
            parts.add(class_5481.method_30747(c.chars, c.style));
        }
        return class_5481.method_30749(parts);
    }

    private static List collectChunks(class_2561 text) {
        List chunks = new ArrayList();
        var tmp0 = new int[1];
        tmp0[0] = 0;
        int[] cursor = tmp0;
        text.method_27658((style, part) -> {
    if (part != null && !(part.isEmpty())) {
        int s = cursor[0];
        int e = (s + part.length());
        chunks.add(new FakeRoles.StyledChunk(part, style, s, e));
        cursor[0] = e;
    }
    return Optional.empty();
}, class_2583.field_24360);
        return chunks;
    }

    private static String rebuild(List chunks) {
        StringBuilder sb = new StringBuilder();
        Object var2 = chunks.iterator();
        while (var2.hasNext()) {
            FakeRoles.StyledChunk c = ((FakeRoles.StyledChunk) var2.next());
            sb.append(c.text);
        }
        return sb.toString();
    }

    private static List collectOrderedChars(class_5481 ot) {
        List chars = new ArrayList();
        var tmp0 = new int[1];
        tmp0[0] = 0;
        int[] pos = tmp0;
        ot.accept((index, style, codePoint) -> {
    0[0][pos] = (0[0] + 1);
    new FakeRoles.OrderedChar(new FakeRoles.OrderedChar, new String(Character.toChars(codePoint)), style);
    null.add(chars);
    return true;
});
        return chars;
    }

    private static int codePointCount(String s) {
        return ((int) s.codePoints().count());
    }


    record StyledChunk(String text, class_2583 style, int start, int end) {

        private StyledChunk(String text, class_2583 style, int start, int end) {
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

    record OrderedChar(String chars, class_2583 style, int globalPos) {

        private OrderedChar(String chars, class_2583 style, int globalPos) {
            this.chars = chars;
            this.style = style;
            this.globalPos = globalPos;
        }

        public String chars() {
            return this.chars;
        }

        public class_2583 style() {
            return this.style;
        }

        public int globalPos() {
            return this.globalPos;
        }

    }

    record ReplacementPart(String text, class_2583 style) {

        private ReplacementPart(String text, class_2583 style) {
            this.text = text;
            this.style = style;
        }

        public String text() {
            return this.text;
        }

        public class_2583 style() {
            return this.style;
        }

    }

    public enum Role {
        OWNER,
        ADMIN,
        MOD,
        HELPER,
        VIP,
        YOUTUBER,
        BUILDER,
        FRIEND,
        CUSTOM;

        private final String prefix;
        private final int defaultColor;

        private Role(String prefix, int defaultColor) {
            this.prefix = prefix;
            this.defaultColor = defaultColor;
        }

        public String prefix() {
            return this.prefix;
        }

        public int defaultColor() {
            return this.defaultColor;
        }

    }
}
