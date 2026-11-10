package me.drake.viecheats.util;

import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.Event;

public final class CoreBridge {
    private static volatile boolean resolved;
    private static Class tickEventClass;
    private static Class screenChangeEventClass;
    private static Class gameJoinEventClass;
    private static Class receivePacketEventClass;
    private static Class sendPacketEventClass;
    private static Class chunkLoadEventClass;
    private static Class renderEventClass;
    private static Class render3DEventClass;
    private static Class playerMoveEventClass;
    private static Class attackEventClass;
    private static Object tickEventPost;
    private static Object attackEventPost;

    private static void ensureResolved() {
        if (!(!resolved)) return;
        Object var0 = CoreBridge.class;
        // synchronized (CoreBridge.class)
        if (!resolved) break label_4;
        return;
        try {
            label_4:
            tickEventClass = Class.forName("me.drake.viecheats.core.event.events.TickEvent");
        } catch (ClassNotFoundException ex6) {
            Object var1 = ex6;
            try {
            }
            screenChangeEventClass = Class.forName("me.drake.viecheats.core.event.events.ScreenChangeEvent");
        } catch (ClassNotFoundException ex9) {
            var1 = ex9;
            try {
            }
            gameJoinEventClass = Class.forName("me.drake.viecheats.core.event.events.GameJoinEvent");
        } catch (ClassNotFoundException ex12) {
            var1 = ex12;
            try {
            }
            receivePacketEventClass = Class.forName("me.drake.viecheats.core.event.events.ReceivePacketEvent");
        } catch (ClassNotFoundException ex15) {
            var1 = ex15;
            try {
            }
            sendPacketEventClass = Class.forName("me.drake.viecheats.core.event.events.SendPacketEvent");
        } catch (ClassNotFoundException ex18) {
            var1 = ex18;
            try {
            }
            chunkLoadEventClass = Class.forName("me.drake.viecheats.core.event.events.ChunkLoadEvent");
        } catch (ClassNotFoundException ex21) {
            var1 = ex21;
            try {
            }
            renderEventClass = Class.forName("me.drake.viecheats.core.event.events.RenderEvent");
        } catch (ClassNotFoundException ex24) {
            var1 = ex24;
            try {
            }
            render3DEventClass = Class.forName("me.drake.viecheats.core.event.events.Render3DEvent");
        } catch (ClassNotFoundException ex27) {
            var1 = ex27;
            try {
            }
            playerMoveEventClass = Class.forName("me.drake.viecheats.core.event.events.PlayerMoveEvent");
        } catch (ClassNotFoundException ex30) {
            var1 = ex30;
            try {
            }
            attackEventClass = Class.forName("me.drake.viecheats.core.event.events.AttackEvent");
        } catch (ClassNotFoundException ex33) {
            var1 = ex33;
            try {
            }
            if (tickEventClass != null) {
                tickEventPost = tickEventClass.getField("POST").get(null);
            }
        } catch (Exception ex36) {
            var1 = ex36;
            try {
            }
            if (attackEventClass != null) {
                attackEventPost = attackEventClass.getField("POST").get(null);
            }
        } catch (Exception ex39) {
            var1 = ex39;
        }
        resolved = true;
        break label_45;
        Object var2 = null;
        throw var2;
        label_45:
    }

    public static boolean isCoreLoaded() {
        ensureResolved();
        return tickEventClass != null;
    }

    public static void postTickEvent() {
        ensureResolved();
        if (tickEventPost == null) {
            return;
            try {
            }
            VieCheatsMod mod = VieCheatsMod.getInstance();
            if (mod != null && mod.getEventBus() != null) {
                mod.getEventBus().post(((Event) tickEventPost));
            }
        } catch (Exception ex6) {
            mod = ex6;
        }
    }

    public static void postScreenChangeEvent(Object screen) {
        ensureResolved();
        if (screenChangeEventClass == null) {
            return;
            try {
            }
            VieCheatsMod mod = VieCheatsMod.getInstance();
            if (mod != null && mod.getEventBus() != null) {
                var tmp0 = new Class[1];
                tmp0[0] = Class.forName("net.minecraft.client.gui.screen.Screen");
                var tmp1 = new Object[1];
                tmp1[0] = screen;
                Event event = ((Event) screenChangeEventClass.getConstructor(tmp0).newInstance(tmp1));
                mod.getEventBus().post(event);
            }
        } catch (Exception ex10) {
            mod = ex10;
        }
    }

    public static void postGameJoinEvent() {
        ensureResolved();
        if (gameJoinEventClass == null) {
            return;
            try {
            }
            VieCheatsMod mod = VieCheatsMod.getInstance();
            if (mod != null && mod.getEventBus() != null) {
                Event event = ((Event) gameJoinEventClass.getConstructor(new Class[0]).newInstance(new Object[0]));
                mod.getEventBus().post(event);
            }
        } catch (Exception ex7) {
            mod = ex7;
        }
    }

    public static boolean postReceivePacketEvent(Object packet) {
        ensureResolved();
        if (receivePacketEventClass == null) {
            return false;
            try {
            }
            VieCheatsMod mod = VieCheatsMod.getInstance();
            if ((mod == null) || (mod.getEventBus() == null)) break label_9;
            var tmp0 = new Class[1];
            tmp0[0] = Object.class;
            var tmp1 = new Object[1];
            tmp1[0] = packet;
            Event event = ((Event) receivePacketEventClass.getConstructor(tmp0).newInstance(tmp1));
            mod.getEventBus().post(event);
            try {
                return ((Boolean) receivePacketEventClass.getMethod("isCancelled", new Class[0]).invoke(event, new Object[0])).booleanValue();
            } catch (NoSuchMethodException ex8) {
                Object var3 = ex8;
                label_9:
            } catch (Exception ex10) {
                mod = ex10;
            }
            return false;
        }
    }

    public static boolean postSendPacketEvent(Object packet) {
        ensureResolved();
        if (sendPacketEventClass == null) {
            return false;
            try {
            }
            VieCheatsMod mod = VieCheatsMod.getInstance();
            if ((mod == null) || (mod.getEventBus() == null)) break label_9;
            var tmp0 = new Class[1];
            tmp0[0] = Object.class;
            var tmp1 = new Object[1];
            tmp1[0] = packet;
            Event event = ((Event) sendPacketEventClass.getConstructor(tmp0).newInstance(tmp1));
            mod.getEventBus().post(event);
            try {
                return ((Boolean) sendPacketEventClass.getMethod("isCancelled", new Class[0]).invoke(event, new Object[0])).booleanValue();
            } catch (NoSuchMethodException ex8) {
                Object var3 = ex8;
                label_9:
            } catch (Exception ex10) {
                mod = ex10;
            }
            return false;
        }
    }

    public static void postChunkLoadEvent(int x, int z) {
        ensureResolved();
        if (chunkLoadEventClass == null) {
            return;
            try {
            }
            VieCheatsMod mod = VieCheatsMod.getInstance();
            if (mod != null && mod.getEventBus() != null) {
                var tmp0 = new Class[2];
                tmp0[0] = Integer.TYPE;
                tmp0[1] = Integer.TYPE;
                var tmp1 = new Object[2];
                tmp1[0] = x;
                tmp1[1] = z;
                Event event = ((Event) chunkLoadEventClass.getConstructor(tmp0).newInstance(tmp1));
                mod.getEventBus().post(event);
            }
        } catch (Exception ex7) {
            mod = ex7;
        }
    }

    public static void postRenderEvent(Object drawContext, float tickDelta) {
        ensureResolved();
        if (renderEventClass == null) {
            return;
            try {
            }
            VieCheatsMod mod = VieCheatsMod.getInstance();
            if (mod != null && mod.getEventBus() != null) {
                var tmp0 = new Class[2];
                tmp0[0] = Object.class;
                tmp0[1] = Float.TYPE;
                var tmp1 = new Object[2];
                tmp1[0] = drawContext;
                tmp1[1] = tickDelta;
                Event event = ((Event) renderEventClass.getConstructor(tmp0).newInstance(tmp1));
                mod.getEventBus().post(event);
            }
        } catch (Exception ex8) {
            mod = ex8;
        }
    }

    public static void postRender3DEvent(Object matrixStack, float tickDelta) {
        ensureResolved();
        if (render3DEventClass == null) {
            return;
            try {
            }
            VieCheatsMod mod = VieCheatsMod.getInstance();
            if (mod != null && mod.getEventBus() != null) {
                var tmp0 = new Class[2];
                tmp0[0] = Object.class;
                tmp0[1] = Float.TYPE;
                var tmp1 = new Object[2];
                tmp1[0] = matrixStack;
                tmp1[1] = tickDelta;
                Event event = ((Event) render3DEventClass.getConstructor(tmp0).newInstance(tmp1));
                mod.getEventBus().post(event);
            }
        } catch (Exception ex8) {
            mod = ex8;
        }
    }

    public static void postPlayerMoveEvent(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        ensureResolved();
        if (playerMoveEventClass == null) {
            return;
            try {
            }
            VieCheatsMod mod = VieCheatsMod.getInstance();
            if (mod != null && mod.getEventBus() != null) {
                var tmp0 = new Class[6];
                tmp0[0] = Double.TYPE;
                tmp0[1] = Double.TYPE;
                tmp0[2] = Double.TYPE;
                tmp0[3] = Float.TYPE;
                tmp0[4] = Float.TYPE;
                tmp0[5] = Boolean.TYPE;
                var tmp1 = new Object[6];
                tmp1[0] = x;
                tmp1[1] = y;
                tmp1[2] = z;
                tmp1[3] = yaw;
                tmp1[4] = pitch;
                tmp1[5] = onGround;
                Event event = ((Event) playerMoveEventClass.getConstructor(tmp0).newInstance(tmp1));
                mod.getEventBus().post(event);
            }
        } catch (Exception ex8) {
            mod = ex8;
        }
    }

    public static void postAttackEvent(Object player, Object target) {
        ensureResolved();
        if (attackEventPost == null) {
            return;
            try {
            }
            VieCheatsMod mod = VieCheatsMod.getInstance();
            if (mod != null && mod.getEventBus() != null) {
                mod.getEventBus().post(((Event) attackEventPost));
            }
        } catch (Exception ex6) {
            mod = ex6;
        }
    }

    public static void postRawEvent(Object event) {
        try {
            VieCheatsMod mod = VieCheatsMod.getInstance();
            if (mod != null && mod.getEventBus() != null) {
                mod.getEventBus().post(((Event) event));
            }
        } catch (Exception ex4) {
            mod = ex4;
        }
    }

    public static Object getModuleManager() {
        try {
            VieCheatsMod mod = VieCheatsMod.getInstance();
            if (mod != null) return VieCheatsMod.class.getMethod("getModuleManager", new Class[0]).invoke(mod, new Object[0]);
        } catch (Exception ex5) {
            mod = ex5;
        }
        return null;
    }

    public static Object getCommandManager() {
        try {
            VieCheatsMod mod = VieCheatsMod.getInstance();
            if (mod != null) return VieCheatsMod.class.getMethod("getCommandManager", new Class[0]).invoke(mod, new Object[0]);
        } catch (Exception ex5) {
            mod = ex5;
        }
        return null;
    }

    static {
        resolved = false;
    }

}
