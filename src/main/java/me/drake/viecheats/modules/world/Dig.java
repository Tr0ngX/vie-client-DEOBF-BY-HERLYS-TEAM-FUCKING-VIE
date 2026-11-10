package me.drake.viecheats.modules.world;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.module.ModuleManager;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.modules.movement.NoFall;

public class Dig extends Module {
    private final BoolSetting instant;
    private final IntSetting mineDelay;
    private final IntSetting yawSpoof;
    private final BoolSetting autoNoFall;
    private boolean noFallWasEnabled;

    public Dig() {
        super("Dig", "Holds attack key while enabled", Category.WORLD);
        this.instant = ((BoolSetting) addSetting(new BoolSetting("Instant", "Teleport player down instantly after block breaks (bypasses fall animation)", false)));
        this.mineDelay = ((IntSetting) addSetting(new IntSetting("MineDelay", "Ticks to wait after landing before mining the next block", 0, 0, 10)));
        this.yawSpoof = ((IntSetting) addSetting(new IntSetting("YawSpoof", "Spoof look direction toward block (0 = off, 1 = down, 2 = down+yaw to block)", 2, 0, 2)));
        this.autoNoFall = ((BoolSetting) addSetting(new BoolSetting("AutoNoFall", "Automatically enable NoFall while Dig is active", true)));
        this.noFallWasEnabled = false;
    }

    public void onEnable() {
        super.onEnable();
        this.noFallWasEnabled = false;
        if ((!(((Boolean) this.autoNoFall.get()).booleanValue())) || (!(isInGame()))) break label_9;
        ModuleManager manager = VieCheatsMod.getInstance().getModuleManager();
        NoFall noFall = ((NoFall) manager.getModule(NoFall.class));
        if (noFall != null && !(noFall.isEnabled())) {
            noFall.setEnabled(true);
            this.noFallWasEnabled = false;
            break label_9;
        }
        this.noFallWasEnabled = true;
        label_9:
    }

    public void onDisable() {
        if (mc != null && mc.field_1690 != null) {
            mc.field_1690.field_1886.method_23481(false);
        }
        if (((!(((Boolean) this.autoNoFall.get()).booleanValue())) || (this.noFallWasEnabled)) || (!(isInGame()))) break label_7;
        ModuleManager manager = VieCheatsMod.getInstance().getModuleManager();
        NoFall noFall = ((NoFall) manager.getModule(NoFall.class));
        if ((noFall == null) || (!(noFall.isEnabled()))) break label_7;
        noFall.setEnabled(false);
        label_7:
        super.onDisable();
    }

    public void onTick() {
        if ((mc == null) || (mc.field_1690 == null)) {
            return;
        }
        if (!(isInGame())) {
            mc.field_1690.field_1886.method_23481(false);
            return;
        }
        mc.field_1690.field_1886.method_23481(true);
    }

    public String getInfo() {
        return "Holding";
    }

}
