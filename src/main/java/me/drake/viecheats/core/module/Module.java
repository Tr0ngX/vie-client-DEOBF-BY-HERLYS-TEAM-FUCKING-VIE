package me.drake.viecheats.core.module;

import java.util.ArrayList;
import java.util.List;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.auth.AuthManager;
import me.drake.viecheats.core.setting.ButtonSetting;
import me.drake.viecheats.core.setting.KeybindSetting;
import me.drake.viecheats.core.setting.Setting;
import net.minecraft.class_2561;
import net.minecraft.class_310;

public abstract class Module {
    protected static final class_310 mc;
    private static final long AUTH_LOCK_NOTICE_COOLDOWN_MS = 2500L;
    private static long lastAuthLockNoticeAtMs;
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private final KeybindSetting keybind;
    private final List settings;
    private final ButtonSetting resetButton;

    protected Module(String name, String description, Category category, int defaultKey) {
        this.enabled = false;
        this.settings = new ArrayList();
        this.name = name;
        this.description = description;
        this.category = category;
        this.keybind = new KeybindSetting("Keybind", ("Toggle key for " + name), defaultKey);
        this.keybind.onChanged(() -> requestConfigSave());
        this.resetButton = new ButtonSetting("Reset", "Reset all settings to default", () -> {
    Object var1 = this.settings.iterator();
    while (var1.hasNext()) {
        Setting s = ((Setting) var1.next());
        s.reset();
    }
});
    }

    protected Module(String name, String description, Category category) {
        this(name, description, category, -1);
    }

    protected Setting addSetting(Setting setting) {
        setting.onChanged(() -> requestConfigSave());
        this.settings.add(setting);
        return setting;
    }

    public void onEnable() {
        VieCheatsMod.getInstance().getEventBus().subscribe(this);
    }

    public void onDisable() {
        VieCheatsMod.getInstance().getEventBus().unsubscribe(this);
    }

    public void onTick() {
    }

    public void onRender(float tickDelta) {
    }

    public String getInfo() {
        return null;
    }

    public boolean isVisibleInClickGui() {
        return true;
    }

    public void toggle() {
        setEnabled(!this.enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        if ((!enabled) || (this.category == Category.CLIENT)) break label_17;
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if (instance == null) {
        } else {
        }
        AuthManager auth = instance.getAuthManager();
        if ((auth == null) || (auth.canEnableProtectedModules())) break label_17;
        auth.requestImmediateValidation();
        if (auth.canEnableProtectedModules()) break label_17;
        if ((mc == null) || (mc.field_1724 == null)) break label_16;
        long now = System.currentTimeMillis();
        if ((now - lastAuthLockNoticeAtMs) < 2500L) break label_16;
        lastAuthLockNoticeAtMs = now;
        mc.field_1724.method_7353(class_2561.method_43470(("\u00a7d[VC]\u00a7c " + auth.getLockMessageForUi())), false);
        label_16:
        return;
        label_17:
        this.enabled = enabled;
        if (!(!enabled)) {
            onEnable();
        } else {
            onDisable();
        }
        instance = VieCheatsMod.getInstance();
        if (instance != null && instance.getHudManager() != null && mc != null && mc.field_1724 != null && mc.field_1687 != null) {
            int color = (!(!enabled) ? -12255352 : -39322);
            String text = ((!(!enabled) ? "Enabled: " : "Disabled: ") + this.name);
            instance.getHudManager().addNotification(text, color);
        }
        requestConfigSave();
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Category getCategory() {
        return this.category;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public KeybindSetting getKeybind() {
        return this.keybind;
    }

    public List getSettings() {
        return this.settings;
    }

    public ButtonSetting getResetButton() {
        return this.resetButton;
    }

    public boolean usesEntityCache() {
        return false;
    }

    protected boolean isInGame() {
        if ((mc == null) || (mc.field_1724 == null)) break label_1;
        return mc.field_1687 != null;
    }

    protected void requestConfigSave() {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getConfigManager() == null)) {
            return;
        }
        instance.getConfigManager().requestSave();
    }

    static {
        mc = class_310.method_1551();
        lastAuthLockNoticeAtMs = 0L;
    }

}
