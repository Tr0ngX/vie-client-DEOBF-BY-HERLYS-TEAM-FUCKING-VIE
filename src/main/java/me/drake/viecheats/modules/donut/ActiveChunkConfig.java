package me.drake.viecheats.modules.donut;

public record ActiveChunkConfig(int activeTimeSeconds, int thresholdMedium, int thresholdHigh, int opacity, boolean chatNotification) {

    public ActiveChunkConfig(int activeTimeSeconds, int thresholdMedium, int thresholdHigh, int opacity, boolean chatNotification) {
        this.activeTimeSeconds = activeTimeSeconds;
        this.thresholdMedium = thresholdMedium;
        this.thresholdHigh = thresholdHigh;
        this.opacity = opacity;
        this.chatNotification = chatNotification;
    }

    public int activeTimeSeconds() {
        return this.activeTimeSeconds;
    }

    public int thresholdMedium() {
        return this.thresholdMedium;
    }

    public int thresholdHigh() {
        return this.thresholdHigh;
    }

    public int opacity() {
        return this.opacity;
    }

    public boolean chatNotification() {
        return this.chatNotification;
    }

}
