package me.drake.viecheats.modules.client;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.EnumSetting;

public class HUD extends Module {
    private final EnumSetting style;

    public HUD() {
        super("HUD", "Master toggle for client HUD panels", Category.CLIENT);
        this.style = ((EnumSetting) addSetting(new EnumSetting("Style", "HUD color style", HUD.Style.DEFAULT)));
    }

    public HUD.Style getStyle() {
        return ((HUD.Style) this.style.get());
    }

    public static HUD get() {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getModuleManager() == null)) {
            return null;
        }
        return ((HUD) instance.getModuleManager().getModule(HUD.class));
    }


    public enum Style {
        DEFAULT,
        WARM,
        COOL;


        private Style() {
        }

    }
}
