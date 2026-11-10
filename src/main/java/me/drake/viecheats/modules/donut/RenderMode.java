package me.drake.viecheats.modules.donut;

public enum RenderMode {
    SHEET,
    PILLAR,
    BOTH;

    private final String label;

    private RenderMode(String label) {
        this.label = label;
    }

    public String label() {
        return this.label;
    }

}
