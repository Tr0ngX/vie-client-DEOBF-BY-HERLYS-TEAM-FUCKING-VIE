package me.drake.viecheats.modules.client;

import java.util.Arrays;
import java.util.Locale;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.StringSetting;
import net.minecraft.class_2561;

public class NameProtect extends Module {
    private final EnumSetting mode;
    private final StringSetting customReplacement;
    private String[] protectedNames;
    private boolean protectSelfForFreecam;
    private static NameProtect INSTANCE;

    public NameProtect() {
        super("NameProtect", "Hides customer names in chat and nametags", Category.CLIENT);
        this.mode = ((EnumSetting) addSetting(new EnumSetting("Mode", "How to hide names", NameProtect.Mode.BLANK, NameProtect.Mode.values())));
        this.customReplacement = ((StringSetting) addSetting(new StringSetting("Replacement", "Text shown instead of hidden names", "???")));
        this.protectedNames = new String[0];
        this.protectSelfForFreecam = false;
        INSTANCE = this;
    }

    public static NameProtect get() {
        return INSTANCE;
    }

    public void setProtectedNames(String[] names) {
        if (names == null) break label_1;
        if (names.length == 0) {
            label_1:
            this.protectedNames = new String[0];
            return;
        }
        String[] normalized = new String[names.length];
        int count = 0;
        Object var4 = names;
        int var5 = var4.length;
        int var6 = 0;
        while (var6 < var5) {
            String name = var4[var6];
            String cleaned = normalizeName(name);
            if (cleaned == null) break label_18;
            if (cleaned.isBlank()) {
                break label_18;
            }
            boolean duplicate = false;
            int i = 0;
            while (i < count) {
                if (cleaned.equals(normalized[i])) {
                    duplicate = true;
                    break label_16;
                }
                i++;
            }
            label_16:
            if (!(!duplicate)) {
                break label_18;
            }
            count++;
            normalized[count] = cleaned;
            label_18:
            var6++;
        }
        this.protectedNames = ((String[]) Arrays.copyOf(normalized, count));
    }

    public String[] getProtectedNames() {
        return this.protectedNames;
    }

    public void onFreecamEnable() {
        this.protectSelfForFreecam = true;
    }

    public void onFreecamDisable() {
        this.protectSelfForFreecam = false;
    }

    private String getReplacement() {
        if (this.mode.get() == NameProtect.Mode.BLANK) return "";
        String repl = ((String) this.customReplacement.get());
        if (repl != null) {
            if (repl.isBlank()) {
            }
        } else {
        }
        return repl;
    }

    private static String normalizeName(String value) {
        if (value == null) return null;
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String[] getActiveProtectedNames() {
        if ((!this.protectSelfForFreecam) || (mc == null)) break label_1;
        if (mc.method_1548() == null) {
            label_1:
            return this.protectedNames;
        }
        String selfName = normalizeName(mc.method_1548().method_1676());
        if ((selfName == null) || (selfName.isBlank())) {
            return this.protectedNames;
        }
        String[] merged = this.protectedNames;
        int var3 = merged.length;
        int var4 = 0;
        while (var4 < var3) {
            String existing = merged[var4];
            if (selfName.equals(normalizeName(existing))) return this.protectedNames;
            var4++;
        }
        merged = ((String[]) Arrays.copyOf(this.protectedNames, (this.protectedNames.length + 1)));
        merged[this.protectedNames.length] = selfName;
        return merged;
    }

    private String replaceImpl(String text) {
        if (text == null) break label_1;
        if (text.isBlank()) {
            label_1:
            return text;
        }
        String[] activeNames = getActiveProtectedNames();
        if (activeNames.length == 0) return text;
        String replacement = getReplacement();
        String lower = text.toLowerCase(Locale.ROOT);
        String result = text;
        Object var6 = activeNames;
        int var7 = var6.length;
        int var8 = 0;
        while (var8 < var7) {
            String rawName = var6[var8];
            String name = normalizeName(rawName);
            if (name == null) break label_18;
            if (name.isBlank()) {
                break label_18;
            }
            int searchFrom = 0;
            label_12:
            var tmp0 = lower.indexOf(name, searchFrom);
            int idx = tmp0;
            if (tmp0 < 0) break label_18;
            result = (result.substring(0, idx) + replacement + result.substring((idx + name.length())));
            lower = result.toLowerCase(Locale.ROOT);
            searchFrom = (idx + replacement.length());
            break label_12;
            label_18:
            var8++;
        }
        return result;
    }

    public String replace(String text) {
        if (!(isEnabled())) return text;
        return replaceImpl(text);
    }

    public String maskSelfName(String originalName) {
        if (!(isEnabled())) return originalName;
        return getReplacement();
    }

    public class_2561 replaceText(class_2561 original) {
        if ((!(isEnabled())) || (original == null)) {
            return original;
        }
        String str = original.getString();
        String replaced = replaceImpl(str);
        if (replaced.equals(str)) return original;
        return class_2561.method_43470(replaced).method_10862(original.method_10866());
    }

    public static String staticReplace(String text) {
        if (INSTANCE != null) {
        } else {
        }
        return text;
    }

    public static class_2561 staticReplaceText(class_2561 original) {
        if (INSTANCE != null) {
        } else {
        }
        return original;
    }

    public static String staticMaskSelfName(String originalName) {
        if (INSTANCE != null) {
        } else {
        }
        return originalName;
    }


    public enum Mode {
        BLANK,
        CUSTOM;


        private Mode() {
        }

    }
}
