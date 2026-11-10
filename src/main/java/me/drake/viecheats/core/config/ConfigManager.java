package me.drake.viecheats.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.Setting;
import me.drake.viecheats.gui.ClickGUI;
import me.drake.viecheats.modules.client.NameProtect;
import net.minecraft.class_310;

public class ConfigManager {
    private static final Gson GSON;
    private static final long AUTOSAVE_DELAY_MS = 1200L;
    private final Path configDir;
    private final Path settingsFile;
    private final Path settingsBackupFile;
    private final Path legacyConfigFile;
    private boolean loading;
    private boolean dirty;
    private long lastDirtyAtMs;

    public ConfigManager() {
        this.loading = false;
        this.dirty = false;
        this.lastDirtyAtMs = 0L;
        this.configDir = class_310.method_1551().field_1697.toPath().resolve("viecheats");
        this.settingsFile = this.configDir.resolve("settings.json");
        this.settingsBackupFile = this.configDir.resolve("settings.backup.json");
        this.legacyConfigFile = this.configDir.resolve("config.json");
    }

    public void save() {
        if (!(!this.loading)) return;
        saveNow();
    }

    public void requestSave() {
        if (!(!this.loading)) return;
        this.dirty = true;
        this.lastDirtyAtMs = System.currentTimeMillis();
    }

    public void tickAutosave() {
        if ((!this.dirty) || (!(!this.loading))) {
            return;
        }
        long now = System.currentTimeMillis();
        if ((now - this.lastDirtyAtMs) < 1200L) return;
        saveNow();
    }

    private void saveNow() {
        try {
            Files.createDirectories(this.configDir, new FileAttribute[0]);
            JsonObject root = new JsonObject();
            JsonObject modulesObj = new JsonObject();
            JsonObject modulesByIdObj = new JsonObject();
            JsonArray friendsArr = VieCheatsMod.getInstance().getModuleManager().getModules().iterator();
            while (friendsArr.hasNext()) {
                Module module = ((Module) friendsArr.next());
                JsonObject modObj = serializeModule(module);
                modulesObj.add(module.getName(), modObj);
                modulesByIdObj.add(moduleConfigId(module), modObj.deepCopy());
            }
            root.add("modules", modulesObj);
            root.add("modulesById", modulesByIdObj);
            friendsArr = new JsonArray();
            module = VieCheatsMod.getInstance().getFriendManager().getFriends().iterator();
            while (module.hasNext()) {
                String friend = ((String) module.next());
                friendsArr.add(friend);
            }
            root.add("friends", friendsArr);
            JsonArray protectedArr = new JsonArray();
            modObj = NameProtect.get().getProtectedNames();
            Writer writer = modObj.length;
            int var8 = 0;
            while (var8 < writer) {
                String n = modObj[var8];
                protectedArr.add(n);
                var8++;
            }
            root.add("protectedNames", protectedArr);
            root.add("clickGui", ClickGUI.exportLayoutState());
            Path tempFile = this.settingsFile.resolveSibling((String.valueOf(this.settingsFile.getFileName()) + ".tmp"));
            writer = new OutputStreamWriter(new FileOutputStream(tempFile.toFile()), StandardCharsets.UTF_8);
            try {
                GSON.toJson(root, writer);
                writer.close();
            } catch (Throwable ex30) {
                var8 = ex30;
                try {
                    writer.close();
                } catch (Throwable ex33) {
                    n = ex33;
                    var8.addSuppressed(n);
                }
                throw var8;
            }
            if (Files.exists(this.settingsFile, new LinkOption[0])) {
                var tmp0 = new CopyOption[1];
                tmp0[0] = StandardCopyOption.REPLACE_EXISTING;
                Files.copy(this.settingsFile, this.settingsBackupFile, tmp0);
            }
            moveAtomicOrReplace(tempFile, this.settingsFile);
            this.dirty = false;
            VieCheatsMod.LOGGER.info("[VieCheat] Settings saved.");
        } catch (IOException ex41) {
            IOException e = ex41;
            VieCheatsMod.LOGGER.error("[VieCheat] Failed to save settings!", e);
        }
    }

    public void load() {
        List candidates = buildLoadCandidates();
        if (candidates.isEmpty()) {
            VieCheatsMod.LOGGER.info("[VieCheat] No settings file found - using defaults.");
            return;
        }
        this.loading = true;
        boolean loaded = false;
        Object var3 = candidates.iterator();
        while (var3.hasNext()) {
            Path candidate = ((Path) var3.next());
            try {
                JsonObject root = readRoot(candidate);
                if (root == null) {
                    break label_7;
                    try {
                    }
                    applyRoot(root);
                    VieCheatsMod.LOGGER.info("[VieCheat] Settings loaded from {}.", candidate.getFileName());
                    loaded = true;
                } catch (Exception ex15) {
                    Exception e = ex15;
                    VieCheatsMod.LOGGER.warn("[VieCheat] Failed to read settings from {}: {}", candidate.getFileName(), e.toString());
                }
            }
            label_18:
            if (!loaded) {
                VieCheatsMod.LOGGER.warn("[VieCheat] Could not load any settings file - keeping defaults.");
            }
            this.loading = false;
            this.dirty = false;
            break label_27;
            Object var6 = null;
            this.loading = false;
            this.dirty = false;
            throw var6;
            label_27:
        }
    }

    public Path getConfigDir() {
        return this.configDir;
    }

    public Path getSettingsFile() {
        return this.settingsFile;
    }

    private JsonObject serializeModule(Module module) {
        JsonObject modObj = new JsonObject();
        modObj.addProperty("name", module.getName());
        modObj.addProperty("category", module.getCategory().name());
        modObj.addProperty("enabled", module.isEnabled());
        modObj.add("keybind", module.getKeybind().toJson());
        JsonObject settingsObj = new JsonObject();
        Object var4 = module.getSettings().iterator();
        while (var4.hasNext()) {
            Setting setting = ((Setting) var4.next());
            settingsObj.add(setting.getName(), setting.toJson());
        }
        modObj.add("settings", settingsObj);
        return modObj;
    }

    private List buildLoadCandidates() {
        List paths = new ArrayList();
        if (Files.exists(this.settingsFile, new LinkOption[0])) {
            paths.add(this.settingsFile);
        }
        if (Files.exists(this.settingsBackupFile, new LinkOption[0])) {
            paths.add(this.settingsBackupFile);
        }
        if (Files.exists(this.legacyConfigFile, new LinkOption[0])) {
            paths.add(this.legacyConfigFile);
        }
        return paths;
    }

    private JsonObject readRoot(Path file) throws IOException {
        Reader reader = new InputStreamReader(new FileInputStream(file.toFile()), StandardCharsets.UTF_8);
        try {
            Object var3 = ((JsonObject) GSON.fromJson(reader, JsonObject.class));
            reader.close();
            return var3;
        } catch (Throwable ex5) {
            var3 = ex5;
            try {
                reader.close();
            } catch (Throwable ex8) {
                Object var4 = ex8;
                var3.addSuppressed(var4);
            }
            throw var3;
        }
    }

    private void applyRoot(JsonObject root) {
        JsonObject modulesByName = root.getAsJsonObject("modules");
        JsonObject modulesById = root.getAsJsonObject("modulesById");
        if (modulesByName != null) break label_3;
        if (modulesById == null) break label_20;
        label_3:
        JsonArray friendsArr = VieCheatsMod.getInstance().getModuleManager().getModules().iterator();
        while (friendsArr.hasNext()) {
            Module module = ((Module) friendsArr.next());
            JsonObject modObj = resolveModuleObject(module, modulesByName, modulesById);
            if (modObj == null) {
                break label_4;
            }
            if (modObj.has("enabled")) {
                module.setEnabled(modObj.get("enabled").getAsBoolean());
            }
            if (modObj.has("keybind")) {
                module.getKeybind().fromJson(modObj.get("keybind"));
            }
            JsonObject settingsObj = modObj.getAsJsonObject("settings");
            if (settingsObj == null) {
                break label_4;
            }
            Object var8 = module.getSettings().iterator();
            while (var8.hasNext()) {
                Setting setting = ((Setting) var8.next());
                JsonElement element = resolveSettingElement(setting, settingsObj);
                if (element != null) {
                    setting.fromJson(element);
                }
            }
        }
        label_20:
        friendsArr = root.getAsJsonArray("friends");
        if (friendsArr == null) break label_26;
        module = friendsArr.iterator();
        while (module.hasNext()) {
            JsonElement el = ((JsonElement) module.next());
            VieCheatsMod.getInstance().getFriendManager().add(el.getAsString());
        }
        label_26:
        JsonArray protectedArr = root.getAsJsonArray("protectedNames");
        if (protectedArr != null) {
            String[] names = new String[protectedArr.size()];
            int i = 0;
            while (i < protectedArr.size()) {
                names[i] = protectedArr.get(i).getAsString();
                i++;
            }
            NameProtect.get().setProtectedNames(names);
        }
        JsonObject clickGuiObj = root.getAsJsonObject("clickGui");
        ClickGUI.importLayoutState(clickGuiObj);
    }

    private JsonObject resolveModuleObject(Module module, JsonObject modulesByName, JsonObject modulesById) {
        if (modulesByName != null) {
            JsonObject byExactName = modulesByName.getAsJsonObject(module.getName());
            if (byExactName != null) return byExactName;
        }
        if (modulesById != null) {
            JsonObject byId = modulesById.getAsJsonObject(moduleConfigId(module));
            if (byId != null) return byId;
        }
        if (modulesByName == null) break label_14;
        String wanted = normalizeKey(module.getName());
        Object var5 = modulesByName.keySet().iterator();
        while (var5.hasNext()) {
            String key = ((String) var5.next());
            if (normalizeKey(key).equals(wanted)) {
                JsonObject loose = modulesByName.getAsJsonObject(key);
                if (loose != null) return loose;
            }
        }
        label_14:
        return null;
    }

    private JsonElement resolveSettingElement(Setting setting, JsonObject settingsObj) {
        if (settingsObj.has(setting.getName())) return settingsObj.get(setting.getName());
        String wanted = normalizeKey(setting.getName());
        Object var4 = settingsObj.keySet().iterator();
        while (var4.hasNext()) {
            String key = ((String) var4.next());
            if (normalizeKey(key).equals(wanted)) return settingsObj.get(key);
        }
        return null;
    }

    private static String moduleConfigId(Module module) {
        return module.getClass().getName();
    }

    private static String normalizeKey(String value) {
        if (value == null) return "";
        String lower = value.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(lower.length());
        int i = 0;
        while (i < lower.length()) {
            char c = lower.charAt(i);
            if (c >= 97) {
                if (c > 122) {
                }
                if (c >= 48 && c <= 57) {
                }
                sb.append(c);
            }
            i++;
        }
        return sb.toString();
    }

    private static void moveAtomicOrReplace(Path from, Path to) throws IOException {
        try {
            var tmp0 = new CopyOption[2];
            tmp0[0] = StandardCopyOption.REPLACE_EXISTING;
            tmp0[1] = StandardCopyOption.ATOMIC_MOVE;
            Files.move(from, to, tmp0);
        } catch (Exception ex2) {
            Exception ex = ex2;
            var tmp1 = new CopyOption[1];
            tmp1[0] = StandardCopyOption.REPLACE_EXISTING;
            Files.move(from, to, tmp1);
        }
    }

    static {
        GSON = new GsonBuilder().setPrettyPrinting().create();
    }

}
