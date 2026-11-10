package me.drake.viecheats.modules.world;

import java.lang.reflect.Field;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.StringSetting;
import net.minecraft.class_498;

public class AutoSign extends Module {
    private final StringSetting line1;
    private final StringSetting line2;
    private final StringSetting line3;
    private final StringSetting line4;
    private final BoolSetting autoClose;

    public AutoSign() {
        super("AutoSign", "Auto-types text on signs", Category.WORLD);
        this.line1 = ((StringSetting) addSetting(new StringSetting("Line1", "First line text", "Vie Cheats")));
        this.line2 = ((StringSetting) addSetting(new StringSetting("Line2", "Second line text", "by Drake")));
        this.line3 = ((StringSetting) addSetting(new StringSetting("Line3", "Third line text", "")));
        this.line4 = ((StringSetting) addSetting(new StringSetting("Line4", "Fourth line text", "")));
        this.autoClose = ((BoolSetting) addSetting(new BoolSetting("AutoClose", "Auto close sign GUI after filling", true)));
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) return;
        if (!(isInGame())) return;
        Field messagesField = mc.field_1755;
        if ((messagesField instanceof class_498)) {
            class_498 signScreen = ((class_498) messagesField);
        } else {
            return;
            try {
            }
            messagesField = class_498.class.getDeclaredField("messages");
            messagesField.setAccessible(true);
            String[] messages = ((String[]) messagesField.get(signScreen));
            if (messages != null) {
                messages[0] = ((String) this.line1.get());
                messages[1] = ((String) this.line2.get());
                messages[2] = ((String) this.line3.get());
                messages[3] = ((String) this.line4.get());
            }
        } catch (NoSuchFieldException | IllegalAccessException ex14) {
            ReflectiveOperationException e = ex14;
            VieCheatsMod.LOGGER.warn("[AutoSign] Could not access sign messages field. Consider using @Accessor mixin.");
        }
        if (((Boolean) this.autoClose.get()).booleanValue()) {
            signScreen.method_25419();
        }
    }

    public String getInfo() {
        return ("\"" + ((String) this.line1.get()) + "\"");
    }

}
