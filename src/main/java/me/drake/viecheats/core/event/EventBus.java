package me.drake.viecheats.core.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import me.drake.viecheats.VieCheatsMod;

public class EventBus {
    private final Map subscriptions;

    public EventBus() {
        this.subscriptions = new ConcurrentHashMap();
    }

    public void subscribe(Object listener) {
        if (listener == null) return;
        Set seenSignatures = new HashSet();
        Class type = listener.getClass();
        while (type != null && type != Object.class) {
            Object var4 = type.getDeclaredMethods();
            int var5 = var4.length;
            int var6 = 0;
            while (var6 < var5) {
                Method method = var4[var6];
                EventHandler annotation = ((EventHandler) method.getAnnotation(EventHandler.class));
                if (annotation == null) {
                    break label_30;
                }
                String signature = (method.getName() + Arrays.toString(method.getParameterTypes()));
                if (!(seenSignatures.add(signature))) {
                    break label_30;
                }
                Class[] params = method.getParameterTypes();
                if (params.length != 1) {
                    VieCheatsMod.LOGGER.warn("[EventBus] @EventHandler method {} has {} params, expected 1. Skipping.", method.getName(), params.length);
                    break label_30;
                }
                if (!(Event.class.isAssignableFrom(params[0]))) {
                    VieCheatsMod.LOGGER.warn("[EventBus] @EventHandler method {} param is not an Event subclass. Skipping.", method.getName());
                    break label_30;
                }
                Class eventType = params[0];
                method.setAccessible(true);
                EventBus.Subscription sub = new EventBus.Subscription(listener, method, annotation.priority());
                ((CopyOnWriteArrayList) this.subscriptions.computeIfAbsent(eventType, (k) -> new CopyOnWriteArrayList())).add(sub);
                CopyOnWriteArrayList list = ((CopyOnWriteArrayList) this.subscriptions.get(eventType));
                List sorted = new ArrayList(list);
                sorted.sort(Comparator.comparingInt(EventBus.Subscription::priority));
                list.clear();
                list.addAll(sorted);
                label_30:
                var6++;
            }
            type = type.getSuperclass();
        }
    }

    public void unsubscribe(Object listener) {
        if (listener == null) return;
        Object var2 = this.subscriptions.values().iterator();
        while (var2.hasNext()) {
            CopyOnWriteArrayList subs = ((CopyOnWriteArrayList) var2.next());
            subs.removeIf((sub) -> sub.listener() == listener);
        }
    }

    public Event post(Event event) {
        CopyOnWriteArrayList subs = ((CopyOnWriteArrayList) this.subscriptions.get(event.getClass()));
        if ((subs == null) || (subs.isEmpty())) {
            return event;
        }
        Object var3 = subs.iterator();
        while (var3.hasNext()) {
            EventBus.Subscription sub = ((EventBus.Subscription) var3.next());
            try {
                var tmp0 = new Object[1];
                tmp0[0] = event;
                sub.method().invoke(sub.listener(), tmp0);
            } catch (Exception ex7) {
                Exception e = ex7;
                var tmp1 = new Object[4];
                tmp1[0] = event.getClass().getSimpleName();
                tmp1[1] = sub.listener().getClass().getSimpleName();
                tmp1[2] = sub.method().getName();
                tmp1[3] = e;
                VieCheatsMod.LOGGER.error("[EventBus] Error dispatching {} to {}.{}", tmp1);
            }
        }
        return event;
    }

    public boolean hasSubscribers(Class eventType) {
        CopyOnWriteArrayList subs = ((CopyOnWriteArrayList) this.subscriptions.get(eventType));
        if (subs == null) break label_2;
        return !(subs.isEmpty());
    }


    record Subscription(Object listener, Method method, int priority) {

        private Subscription(Object listener, Method method, int priority) {
            this.listener = listener;
            this.method = method;
            this.priority = priority;
        }

        public Object listener() {
            return this.listener;
        }

        public Method method() {
            return this.method;
        }

        public int priority() {
            return this.priority;
        }

    }
}
