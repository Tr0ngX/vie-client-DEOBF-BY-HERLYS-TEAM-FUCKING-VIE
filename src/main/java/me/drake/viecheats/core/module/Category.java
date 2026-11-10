package me.drake.viecheats.core.module;

public enum Category {
    COMBAT,
    MOVEMENT,
    PLAYER,
    RENDER,
    WORLD,
    EXPLOIT,
    DONUT_SMP,
    CLIENT;

    private final String displayName;
    private final String icon;

    private Category(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getIcon() {
        return this.icon;
    }

}
