package me.drake.viecheats.modules.combat;

import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.ReceivePacketEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.util.ServerSafety;
import net.minecraft.class_243;
import net.minecraft.class_2743;

public class Velocity extends Module {
    private final EnumSetting mode;
    private final DoubleSetting horizontal;
    private final DoubleSetting vertical;
    private boolean gotKnockback;
    private int kbTicksRemaining;

    public Velocity() {
        super("Velocity", "Reduces knockback received", Category.COMBAT);
        this.mode = ((EnumSetting) addSetting(new EnumSetting("Mode", "Velocity mode", Velocity.Mode.LEGIT, Velocity.Mode.values())));
        this.horizontal = ((DoubleSetting) addSetting(new DoubleSetting("Horizontal", "Horizontal velocity multiplier", 0.3, 0.0, 1.0)));
        this.vertical = ((DoubleSetting) addSetting(new DoubleSetting("Vertical", "Vertical velocity multiplier", 0.85, 0.0, 1.0)));
        this.gotKnockback = false;
        this.kbTicksRemaining = 0;
    }

    @EventHandler
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!(isInGame())) return;
        class_243 vel = event.getPacket();
        if ((vel instanceof class_2743)) {
            class_2743 pkt = ((class_2743) vel);
        } else {
            return;
        }
        if (pkt.method_11818() != mc.field_1724.method_5628()) return;
        if (ServerSafety.isDonutServer() && this.mode.get() != Velocity.Mode.JUMP_RESET) return;
        switch (((Velocity.Mode) this.mode.get()).ordinal()) {
            case 0 -> { break label_8; }
            case 1 -> { break label_15; }
            case 2 -> { break label_26; }
            default -> { break label_28; }
        }
        label_8:
        event.cancel();
        vel = pkt.method_73085();
        double vx = ((vel.field_1352 / 8000.0) * ((Double) this.horizontal.get()).doubleValue());
        double vy = ((vel.field_1351 / 8000.0) * ((Double) this.vertical.get()).doubleValue());
        double vz = ((vel.field_1350 / 8000.0) * ((Double) this.horizontal.get()).doubleValue());
        mc.field_1724.method_18800(vx, vy, vz);
        break label_28;
        label_15:
        event.cancel();
        vel = pkt.method_73085();
        double initH = Math.min(1.0, (((Double) this.horizontal.get()).doubleValue() + 0.3));
        double initV = Math.min(1.0, (((Double) this.vertical.get()).doubleValue() + 0.1));
        vx = ((vel.field_1352 / 8000.0) * initH);
        vy = ((vel.field_1351 / 8000.0) * initV);
        vz = ((vel.field_1350 / 8000.0) * initH);
        mc.field_1724.method_18800(vx, vy, vz);
        this.gotKnockback = true;
        this.kbTicksRemaining = 3;
        break label_28;
        label_26:
        this.gotKnockback = true;
        this.kbTicksRemaining = 2;
        label_28:
    }

    public void onTick() {
        if (!(isInGame())) return;
        if (ServerSafety.isDonutServer() && this.mode.get() != Velocity.Mode.JUMP_RESET) {
            this.gotKnockback = false;
            return;
        }
        if (!this.gotKnockback) return;
        this.kbTicksRemaining = (this.kbTicksRemaining - 1);
        switch (((Velocity.Mode) this.mode.get()).ordinal()) {
            case 1 -> { break label_7; }
            case 2 -> { break label_16; }
            default -> { break label_21; }
        }
        label_7:
        if (this.kbTicksRemaining > 0) {
            class_243 vel = mc.field_1724.method_18798();
            mc.field_1724.method_18800((vel.field_1352 * ((Double) this.horizontal.get()).doubleValue()), vel.field_1351, (vel.field_1350 * ((Double) this.horizontal.get()).doubleValue()));
            break label_22;
        }
        this.gotKnockback = false;
        break label_22;
        label_16:
        if (this.kbTicksRemaining <= 0 && mc.field_1724.method_24828()) {
            mc.field_1724.method_6043();
            this.gotKnockback = false;
        }
        if (this.kbTicksRemaining >= -5) break label_22;
        this.gotKnockback = false;
        break label_22;
        label_21:
        this.gotKnockback = false;
        label_22:
    }

    public String getInfo() {
        return ((Velocity.Mode) this.mode.get()).name();
    }


    public enum Mode {
        REDUCE,
        LEGIT,
        JUMP_RESET;


        private Mode() {
        }

    }
}
