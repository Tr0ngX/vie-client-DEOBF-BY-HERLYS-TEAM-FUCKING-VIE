package me.drake.viecheats.modules.movement;

import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import net.minecraft.class_3532;
import net.minecraft.class_5498;
import org.lwjgl.glfw.GLFW;

public final class FreeLook extends Module {
    private final EnumSetting mode;
    private final BoolSetting togglePerspective;
    private final DoubleSetting sensitivity;
    private final BoolSetting arrows;
    private final DoubleSetting arrowSpeed;
    public float cameraYaw;
    public float cameraPitch;
    private class_5498 prePerspective;

    public FreeLook() {
        super("Free Look", "Allows more rotation options in third person.", Category.MOVEMENT);
        this.mode = ((EnumSetting) addSetting(new EnumSetting("Mode", "FreeLook rotation mode.", FreeLook.Mode.CAMERA)));
        this.togglePerspective = ((BoolSetting) addSetting(new BoolSetting("Toggle Perspective", "Switch to third person when enabled.", true)));
        this.sensitivity = ((DoubleSetting) addSetting(new DoubleSetting("Sensitivity", "Mouse sensitivity multiplier.", 1.0, 1.0, 10.0, 0.1)));
        this.arrows = ((BoolSetting) addSetting(new BoolSetting("Arrows Control Opposite", "Arrow keys rotate opposite target.", true)));
        this.arrowSpeed = ((DoubleSetting) addSetting(new DoubleSetting("Arrow Speed", "Arrow rotation speed.", 10.0, 4.0, 10.0, 0.1)));
        this.prePerspective = class_5498.field_26664;
    }

    public void onEnable() {
        super.onEnable();
        if (mc.field_1724 == null) return;
        this.cameraYaw = mc.field_1724.method_36454();
        this.cameraPitch = mc.field_1724.method_36455();
        this.prePerspective = mc.field_1690.method_31044();
        if (this.prePerspective != class_5498.field_26665 && ((Boolean) this.togglePerspective.get()).booleanValue()) {
            mc.field_1690.method_31043(class_5498.field_26665);
        }
    }

    public void onDisable() {
        super.onDisable();
        if (mc.field_1724 == null) return;
        if (mc.field_1690.method_31044() != this.prePerspective && ((Boolean) this.togglePerspective.get()).booleanValue()) {
            mc.field_1690.method_31043(this.prePerspective);
        }
    }

    public boolean playerMode() {
        if ((!(isEnabled())) || (mc.field_1690.method_31044() != class_5498.field_26665)) break label_3;
        return this.mode.get() == FreeLook.Mode.PLAYER;
    }

    public boolean cameraMode() {
        if (!(isEnabled())) break label_1;
        return this.mode.get() == FreeLook.Mode.CAMERA;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.POST) return;
        if (mc.field_1724 == null) return;
        if (!(((Boolean) this.arrows.get()).booleanValue())) return;
        long window = mc.method_22683().method_4490();
        int steps = ((int) (((Double) this.arrowSpeed.get()).doubleValue() * 2.0));
        int i = 0;
        while (i < steps) {
            if (this.mode.get() == FreeLook.Mode.PLAYER) {
                if (isKeyPressed(window, 263)) {
                    this.cameraYaw = (this.cameraYaw - 0.5f);
                }
                if (isKeyPressed(window, 262)) {
                    this.cameraYaw = (this.cameraYaw + 0.5f);
                }
                if (isKeyPressed(window, 265)) {
                    this.cameraPitch = (this.cameraPitch - 0.5f);
                }
                if (!(isKeyPressed(window, 264))) break label_20;
                this.cameraPitch = (this.cameraPitch + 0.5f);
                break label_20;
            }
            float yaw = mc.field_1724.method_36454();
            float pitch = mc.field_1724.method_36455();
            if (isKeyPressed(window, 263)) {
                yaw = (yaw - 0.5f);
            }
            if (isKeyPressed(window, 262)) {
                yaw = (yaw + 0.5f);
            }
            if (isKeyPressed(window, 265)) {
                pitch = (pitch - 0.5f);
            }
            if (isKeyPressed(window, 264)) {
                pitch = (pitch + 0.5f);
            }
            mc.field_1724.method_36456(yaw);
            mc.field_1724.method_36457(pitch);
            label_20:
            i++;
        }
        mc.field_1724.method_36457(class_3532.method_15363(mc.field_1724.method_36455(), -90.0f, 90.0f));
        this.cameraPitch = class_3532.method_15363(this.cameraPitch, -90.0f, 90.0f);
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        double sens = (((Double) this.sensitivity.get()).doubleValue() * 0.5);
        this.cameraYaw = (this.cameraYaw + ((float) (deltaX * sens)));
        this.cameraPitch = (this.cameraPitch + ((float) (deltaY * sens)));
        this.cameraYaw = class_3532.method_15393(this.cameraYaw);
        this.cameraPitch = class_3532.method_15363(this.cameraPitch, -90.0f, 90.0f);
    }

    public void applyMouseDelta(float deltaYaw, float deltaPitch) {
        changeLookDirection(((double) deltaYaw), ((double) deltaPitch));
    }

    public boolean isPlayerMode() {
        return playerMode();
    }

    public boolean isCameraMode() {
        return cameraMode();
    }

    public boolean isRenderAllActive() {
        return cameraMode();
    }

    private static boolean isKeyPressed(long window, int key) {
        return GLFW.glfwGetKey(window, key) == 1;
    }


    public enum Mode {
        PLAYER,
        CAMERA;


        private Mode() {
        }

    }
}
