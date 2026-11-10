package me.drake.viecheats.modules.render;

import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.ColorSetting;
import me.drake.viecheats.core.setting.IntSetting;

public class SpotifyHUD extends Module {
    public final IntSetting scale;
    public final ColorSetting accentColor;
    public final IntSetting posX;
    public final IntSetting posY;

    public SpotifyHUD() {
        super("SpotifyHUD", "Spotify music player overlay", Category.RENDER);
        this.scale = ((IntSetting) addSetting(new IntSetting("Scale", "HUD scale (1=default)", 9, 5, 20, 1)));
        this.accentColor = ((ColorSetting) addSetting(new ColorSetting("AccentColor", "Accent color for the HUD", -11544065)));
        this.posX = ((IntSetting) addSetting(new IntSetting("PosX", "", 10, 0, 10000, 1)));
        this.posY = ((IntSetting) addSetting(new IntSetting("PosY", "", 10, 0, 10000, 1)));
        this.accentColor.setVisibleInGui(false);
        this.posX.setVisibleInGui(false);
        this.posY.setVisibleInGui(false);
    }

    public int getAccentColor() {
        return ((Integer) this.accentColor.get()).intValue();
    }

    public float getScale() {
        return (((float) ((Integer) this.scale.get()).intValue()) / 10.0f);
    }

    public int getPosX() {
        return ((Integer) this.posX.get()).intValue();
    }

    public int getPosY() {
        return ((Integer) this.posY.get()).intValue();
    }

}
