package me.drake.viecheats.modules.donut;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.core.setting.KeybindSetting;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import org.lwjgl.glfw.GLFW;

public class SpawnerBreaker extends Module {
    private final KeybindSetting key;
    private final IntSetting radius;
    private final BoolSetting notify;
    private boolean wasKeyDown;

    public SpawnerBreaker() {
        super("SpawnerBreaker", "Breaks all spawners in radius", Category.DONUT_SMP);
        this.key = ((KeybindSetting) addSetting(new KeybindSetting("Key", "Key to break spawners")));
        this.radius = ((IntSetting) addSetting(new IntSetting("Radius", "Scan radius in blocks", 4, 1, 6)));
        this.notify = ((BoolSetting) addSetting(new BoolSetting("Notify", "Send chat message on break", true)));
        this.wasKeyDown = false;
    }

    public void onTick() {
        if (!(isInGame())) break label_1;
        if (!(this.key.isBound())) {
            label_1:
            return;
        }
        if (((Integer) this.key.get()).intValue() >= 1000) {
            if (GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), (((Integer) this.key.get()).intValue() - 1000)) == 1) {
                break label_7;
            }
            break label_7;
        }
        boolean keyDown = GLFW.glfwGetKey(mc.method_22683().method_4490(), ((Integer) this.key.get()).intValue()) == 1;
        if (!(!keyDown) && !this.wasKeyDown) {
            breakSpawners();
        }
        this.wasKeyDown = keyDown;
    }

    private void breakSpawners() {
        if (mc.field_1724 == null) break label_1;
        if (mc.field_1687 != null) break label_2;
        label_1:
        return;
        label_2:
        class_2338 playerPos = mc.field_1724.method_24515();
        int r = ((Integer) this.radius.get()).intValue();
        int broken = 0;
        int x = -(r);
        while (x <= r) {
            int y = -(r);
            while (y <= r) {
                int z = -(r);
                while (z <= r) {
                    class_2338 target = playerPos.method_10069(x, y, z);
                    if (mc.field_1687.method_8320(target).method_27852(class_2246.field_10260)) {
                        mc.field_1687.method_8651(target, true, mc.field_1724);
                        broken++;
                    }
                    z++;
                }
                y++;
            }
            x++;
        }
        if (!(((Boolean) this.notify.get()).booleanValue())) break label_26;
        if (broken > 0) {
            mc.field_1724.method_7353(class_2561.method_43470(("\u00a7a[SpawnerBreaker] \u00a7fBroke \u00a7c" + broken + "\u00a7f spawner(s)!")), false);
            break label_26;
        }
        mc.field_1724.method_7353(class_2561.method_43470("\u00a7c[SpawnerBreaker] \u00a7fNo spawners in range."), false);
        label_26:
    }

    public String getInfo() {
        if (this.key.isBound()) {
        } else {
        }
        return "NONE";
    }

}
