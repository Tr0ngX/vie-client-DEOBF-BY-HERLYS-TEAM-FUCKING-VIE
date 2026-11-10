package me.drake.viecheats.util;

import java.util.concurrent.ConcurrentLinkedQueue;

public final class ChatMessageQueue {
    private static final ConcurrentLinkedQueue QUEUE;

    public static void push(String message) {
        if (message != null && !(message.isEmpty())) {
            QUEUE.offer(message);
        }
    }

    public static String poll() {
        return ((String) QUEUE.poll());
    }

    public static boolean isEmpty() {
        return QUEUE.isEmpty();
    }

    static {
        QUEUE = new ConcurrentLinkedQueue();
    }

}
