package me.drake.viecheats.modules.donut;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.core.setting.StringSetting;

public class EmergencySeller extends Module {
    private final StringSetting cmd1;
    private final StringSetting cmd2;
    private final StringSetting cmd3;
    private final IntSetting cmdDelay;
    private boolean triggered;
    private int cmdIndex;
    private int delayTicks;

    public EmergencySeller() {
        super("EmergencySeller", "Panic sell \u2014 runs cmds instantly (DonutSMP)", Category.DONUT_SMP);
        this.cmd1 = ((StringSetting) addSetting(new StringSetting("PanicCmd1", "Emergency sell command 1", "/sell all")));
        this.cmd2 = ((StringSetting) addSetting(new StringSetting("PanicCmd2", "Emergency sell command 2", "")));
        this.cmd3 = ((StringSetting) addSetting(new StringSetting("PanicCmd3", "Emergency sell command 3", "")));
        this.cmdDelay = ((IntSetting) addSetting(new IntSetting("CmdDelay", "Ticks between commands", 5, 1, 20)));
        this.triggered = false;
        this.cmdIndex = 0;
        this.delayTicks = 0;
    }

    public void onEnable() {
        super.onEnable();
        this.triggered = true;
        this.cmdIndex = 0;
        this.delayTicks = 0;
    }

    public void onTick() {
        if (!(isInGame())) break label_1;
        if (!this.triggered) {
            label_1:
            return;
        }
        if (this.delayTicks > 0) {
            this.delayTicks = (this.delayTicks - 1);
            return;
        }
        var tmp0 = new String[3];
        tmp0[0] = ((String) this.cmd1.get());
        tmp0[1] = ((String) this.cmd2.get());
        tmp0[2] = ((String) this.cmd3.get());
        String[] cmds = tmp0;
        label_4:
        if (this.cmdIndex < cmds.length) {
            this.cmdIndex = (this.cmdIndex + 1);
            String cmd = cmds[this.cmdIndex];
            if (cmd == null) break label_4;
            if (cmd.isBlank()) {
                break label_4;
            }
            sendCmd(cmd);
            this.delayTicks = ((Integer) this.cmdDelay.get()).intValue();
            return;
        }
        this.triggered = false;
        setEnabled(false);
    }

    private void sendCmd(String cmd) {
        if (cmd.startsWith("/")) {
            mc.field_1724.field_3944.method_45730(cmd.substring(1));
        } else {
            mc.field_1724.field_3944.method_45729(cmd);
        }
    }

}
