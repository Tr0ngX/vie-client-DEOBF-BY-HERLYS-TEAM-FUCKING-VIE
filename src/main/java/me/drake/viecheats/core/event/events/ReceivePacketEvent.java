package me.drake.viecheats.core.event.events;

import me.drake.viecheats.core.event.CancellableEvent;
import net.minecraft.class_2596;

public class ReceivePacketEvent extends CancellableEvent {
    private final class_2596 packet;

    public ReceivePacketEvent(class_2596 packet) {
        this.packet = packet;
    }

    public class_2596 getPacket() {
        return this.packet;
    }

}
