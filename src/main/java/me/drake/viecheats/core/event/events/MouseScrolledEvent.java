package me.drake.viecheats.core.event.events;

import me.drake.viecheats.core.event.Event;

public class MouseScrolledEvent extends Event {
    private final double amount;

    public MouseScrolledEvent(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return this.amount;
    }

}
