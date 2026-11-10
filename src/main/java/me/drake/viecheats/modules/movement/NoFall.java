package me.drake.viecheats.modules.movement;

import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.SendPacketEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.util.ServerSafety;
import net.minecraft.class_2338;
import net.minecraft.class_2828;

public class NoFall extends Module {
    private final EnumSetting mode;
    private final DoubleSetting fallThreshold;
    private boolean spoofing;

    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.MOVEMENT);
        this.mode = ((EnumSetting) addSetting(new EnumSetting("Mode", "NoFall mode", NoFall.Mode.PACKET, NoFall.Mode.values())));
        this.fallThreshold = ((DoubleSetting) addSetting(new DoubleSetting("Threshold", "Fall distance to activate", 3.0, 1.0, 20.0)));
        this.spoofing = false;
    }

    public void onTick() {
        if (!(isInGame())) return;
        if (ServerSafety.isDonutServer()) {
            if (mc.field_1724.field_6017 > ((Double) this.fallThreshold.get()).doubleValue()) {
                mc.field_1724.field_6017 = 0.0;
            }
            return;
        }
        if (this.mode.get() == NoFall.Mode.SPOOF && mc.field_1724.field_6017 > ((Double) this.fallThreshold.get()).doubleValue()) {
            mc.field_1724.field_6017 = 0.0;
        }
    }

    @EventHandler
    public void onSendPacket(SendPacketEvent event) {
        if (!(isInGame())) return;
        if (ServerSafety.isDonutServer()) return;
        if (this.mode.get() != NoFall.Mode.PACKET) return;
        if (!(!this.spoofing)) return;
        double fallDist = event.getPacket();
        if ((fallDist instanceof class_2828)) {
            class_2828 packet = ((class_2828) fallDist);
        } else {
            return;
        }
        fallDist = mc.field_1724.field_6017;
        if (fallDist > ((Double) this.fallThreshold.get()).doubleValue() && isNearGround()) {
            this.spoofing = true;
            event.cancel();
            mc.method_1562().method_52787(new class_2828.class_2830(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321(), mc.field_1724.method_36454(), mc.field_1724.method_36455(), true, false));
            this.spoofing = false;
        }
    }

    private boolean isNearGround() {
        int i = 0;
        while (i < 3) {
            class_2338 pos = mc.field_1724.method_24515().method_10087((i + 1));
            if (!(mc.field_1687.method_8320(pos).method_26215())) return true;
            i++;
        }
        return false;
    }

    public String getInfo() {
        return ((NoFall.Mode) this.mode.get()).name();
    }


    public enum Mode {
        PACKET,
        SPOOF;


        private Mode() {
        }

    }
}
