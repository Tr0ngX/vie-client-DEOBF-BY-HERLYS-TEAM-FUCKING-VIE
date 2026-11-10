package me.drake.viecheats.modules.player;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import net.minecraft.class_2561;
import net.minecraft.class_634;

public class HomeSetter extends Module {
    private final IntSetting homeSlot;
    private final IntSetting delayMs;
    private final BoolSetting chatFeedback;
    private static boolean running;
    private static long executeSetHomeAtMs;
    private static int slot;

    public HomeSetter() {
        super("HomeSetter", "Auto delete and set home at current position", Category.PLAYER);
        this.homeSlot = ((IntSetting) addSetting(new IntSetting("Home Slot", "Slot number to set", 1, 1, 10)));
        this.delayMs = ((IntSetting) addSetting(new IntSetting("Delay ms", "Delay before sethome", 200, 0, 5000)));
        this.chatFeedback = ((BoolSetting) addSetting(new BoolSetting("Chat Feedback", "Show confirmation message", true)));
    }

    public void onEnable() {
        super.onEnable();
        if (!running) {
            if (!(isInGame())) {
            }
            setEnabled(false);
            return;
        }
        slot = ((Integer) this.homeSlot.get()).intValue();
        int waitMs = Math.max(0, ((Integer) this.delayMs.get()).intValue());
        executeSetHomeAtMs = (System.currentTimeMillis() + ((long) waitMs));
        running = true;
        sendCommand(("/delhome " + slot));
    }

    public void onDisable() {
        running = false;
        executeSetHomeAtMs = 0L;
        slot = 0;
        super.onDisable();
    }

    public void onTick() {
        if (!(!running)) {
            if (!(isInGame())) {
            }
            if (!(!running)) {
                running = false;
                setEnabled(false);
            }
            return;
        }
        if (System.currentTimeMillis() < executeSetHomeAtMs) return;
        sendCommand(("/sethome " + slot));
        if (((Boolean) this.chatFeedback.get()).booleanValue() && mc.field_1724 != null) {
            mc.field_1724.method_7353(class_2561.method_43470(("\u00a7aHome " + slot + " deleted and set successfully!")), false);
        }
        running = false;
        setEnabled(false);
    }

    private void sendCommand(String command) {
        if (mc == null) return;
        class_634 handler = null;
        if (mc.field_1724 != null) {
            handler = mc.field_1724.field_3944;
        }
        if (handler == null) {
            handler = mc.method_1562();
        }
        if (handler == null) return;
        if (command.startsWith("/")) {
        } else {
        }
        String cmd = command;
        try {
            handler.method_45730(cmd);
        } catch (Throwable ex10) {
            Throwable ignored = ex10;
            try {
                handler.method_45729(command);
            } catch (Throwable ex13) {
                Object var5 = ex13;
            }
        }
        label_14:
    }

}
