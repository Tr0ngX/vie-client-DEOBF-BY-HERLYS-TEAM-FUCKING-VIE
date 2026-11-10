package me.drake.viecheats.util;

public class TimerUtil {
    private int ticks;

    public TimerUtil() {
        this.ticks = 0;
    }

    public void tick() {
        this.ticks = (this.ticks + 1);
    }

    public boolean hasElapsed(int delay) {
        return this.ticks >= delay;
    }

    public void reset() {
        this.ticks = 0;
    }

    public int getTicks() {
        return this.ticks;
    }

    public boolean checkAndReset(int delay) {
        if (this.ticks >= delay) {
            this.ticks = 0;
            return true;
        }
        return false;
    }

}
