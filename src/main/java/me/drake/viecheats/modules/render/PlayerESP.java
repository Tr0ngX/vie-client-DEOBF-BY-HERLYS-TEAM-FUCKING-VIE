package me.drake.viecheats.modules.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.core.setting.Setting;
import me.drake.viecheats.render.TessellatorRenderer;
import me.drake.viecheats.util.ClickGuiTheme;
import me.drake.viecheats.util.EntityCache;
import me.drake.viecheats.util.PerformanceHelper;
import me.drake.viecheats.util.RainbowUtil;
import me.drake.viecheats.util.RenderUtil;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_640;

public class PlayerESP extends Module {
    private final BoolSetting showInvisible;
    private final BoolSetting showHUD;
    private final BoolSetting tracers;
    private final BoolSetting onlyVisible;
    private final IntSetting range;
    private final IntSetting maxPlayers;
    private static final int COLOR = -4160260;
    private static final int FRIEND_COLOR = -16711800;
    private static final int ENEMY_COLOR = -48026;
    private static final int OUTLINE_ALPHA = 200;
    private static final int FILL_ALPHA = 35;
    private static final int TRACER_ALPHA = 190;
    private static final double RAINBOW_SPEED = 1.2;
    private static final double RAINBOW_SAT = 0.85;
    private static final double PULSE_SPEED = 1.5;
    private static final int PANEL_W = 230;
    private static final int HEADER_H = 22;
    private static final int ROW_H = 30;
    private static final int HUD_BOTTOM_OFFSET = 126;
    private final List nearbyPlayers;
    private int cacheTimer;

    public PlayerESP() {
        super("PlayerESP", "Highlights players through walls", Category.RENDER);
        this.showInvisible = ((BoolSetting) addSetting(new BoolSetting("ShowInvisible", "Show invisible players", true)));
        this.showHUD = ((BoolSetting) addSetting(new BoolSetting("ShowHUD", "Show nearby player HUD panel", true)));
        this.tracers = ((BoolSetting) addSetting(new BoolSetting("Tracers", "Draw tracers to players", true)));
        this.onlyVisible = ((BoolSetting) addSetting(new BoolSetting("OnlyVisible", "Only render visible players", true)));
        this.range = ((IntSetting) addSetting(((IntSetting) hidden(new IntSetting("Range", "Max render range", 128, 24, 192)))));
        this.maxPlayers = ((IntSetting) addSetting(((IntSetting) hidden(new IntSetting("MaxPlayers", "Max players rendered per frame", 32, 4, 256)))));
        this.nearbyPlayers = new ArrayList();
        this.cacheTimer = 0;
    }

    private static Setting hidden(Setting setting) {
        setting.setVisibleInGui(false);
        return setting;
    }

    public String getInfo() {
        return (this.nearbyPlayers.size() + " near");
    }

    public boolean usesEntityCache() {
        return true;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) break label_1;
        if (isInGame()) break label_2;
        label_1:
        return;
        label_2:
        this.cacheTimer = (this.cacheTimer + 1);
        if ((this.cacheTimer + 1) < 10) return;
        this.cacheTimer = 0;
        this.nearbyPlayers.clear();
        Object var2 = mc.field_1687.method_18456().iterator();
        while (var2.hasNext()) {
            class_1657 player = ((class_1657) var2.next());
            if (player == mc.field_1724) {
                break label_6;
            }
            if (player.method_5628() == -6969) {
                break label_6;
            }
            if (!(((Boolean) this.showInvisible.get()).booleanValue()) && player.method_5767()) {
                break label_6;
            }
            double dist = ((double) mc.field_1724.method_5739(player));
            String name = player.method_5477().getString();
            float hp = player.method_6032();
            float maxHp = player.method_6063();
            int armor = player.method_6096();
            boolean friend = VieCheatsMod.getInstance().getFriendManager().isFriend(name);
            int ping = -1;
            if (mc.method_1562() == null) break label_20;
            class_640 entry = mc.method_1562().method_2871(player.method_5667());
            if (entry == null) break label_20;
            ping = entry.method_2959();
            label_20:
            if (player.method_6047().method_7960()) {
            } else {
            }
            String held = player.method_6047().method_7909().method_63680().getString();
            this.nearbyPlayers.add(new PlayerESP.NearbyPlayer(name, dist, hp, maxHp, armor, ping, held, friend, player.method_5767()));
        }
        this.nearbyPlayers.sort(Comparator.comparingDouble((p) -> p.dist));
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (!(isInGame())) return;
        if (event.getStage() == RenderEvent.Stage.WORLD_3D) {
            render3D(event);
            break label_8;
        }
        if ((event.getStage() != RenderEvent.Stage.HUD_2D) || (!(((Boolean) this.showHUD.get()).booleanValue()))) break label_8;
        class_332 ctx = event.getDrawContext();
        if (ctx == null) return;
        if (!(TessellatorRenderer.isInFrame())) return;
        renderHUD(ctx);
        label_8:
    }

    private void render3D(RenderEvent event) {
        float tickDelta = event.getTickDelta();
        double effectiveRange = ((double) ((Integer) this.range.get()).intValue());
        double maxDistSq = (effectiveRange * effectiveRange);
        int renderCap = PerformanceHelper.scaleRenderCap(((Integer) this.maxPlayers.get()).intValue());
        int rendered = 0;
        Object var9 = EntityCache.getEntities().iterator();
        while (var9.hasNext()) {
            EntityCache.CachedEntity ce = ((EntityCache.CachedEntity) var9.next());
            double distSq = ce.distanceSq();
            if (distSq > maxDistSq) {
                break label_32;
            }
            class_238 box = ce.entity();
            if (!((box instanceof class_1657))) break label_6;
            class_1657 player = ((class_1657) box);
            if (player == mc.field_1724) {
                break label_6;
            }
            if (player.method_5628() == -6969) {
                break label_6;
            }
            if (!(((Boolean) this.showInvisible.get()).booleanValue()) && player.method_5767()) {
                break label_6;
            }
            box = RenderUtil.getInterpolatedBox(player, tickDelta);
            boolean visible = EntityCache.isVisible(box);
            if (((Boolean) this.onlyVisible.get()).booleanValue() && !visible) {
                break label_6;
            }
            boolean friend = isFriend(player.method_5477().getString());
            if (!(!friend)) {
            } else {
            }
            int baseCol = resolveColor(ce.distanceSq(), ce.entity().method_5628());
            int outlineCol = RainbowUtil.withAlpha(baseCol, 200);
            outlineCol = RainbowUtil.pulse(outlineCol, 1.5, 66, 200);
            int fillCol = RainbowUtil.withAlpha(baseCol, 35);
            int tracerCol = RainbowUtil.withAlpha(baseCol, 190);
            if (((Boolean) this.onlyVisible.get()).booleanValue()) {
                if (!(!visible)) {
                }
                RenderUtil.drawBox(box, outlineCol);
                RenderUtil.drawFilledBox(box, fillCol);
            }
            if (((Boolean) this.tracers.get()).booleanValue()) {
                RenderUtil.drawTracer(player, tracerCol, tickDelta);
            }
            rendered++;
            if (rendered >= renderCap) {
                break label_32;
            }
        }
        label_32:
    }

    private void renderHUD(class_332 ctx) {
        if ((ctx == null) || (this.nearbyPlayers.isEmpty())) {
            return;
        }
        class_327 tr = mc.field_1772;
        int screenH = mc.method_22683().method_4502();
        ClickGuiTheme.Palette palette = ClickGuiTheme.resolve();
        int accent = palette.accent();
        int accentSoft = palette.accentSoft();
        int bg = palette.bg();
        int rowBg = palette.rowBg();
        int rowBorder = palette.rowBorder();
        int tglOff = palette.tglOff();
        int txtOn = palette.txtOn();
        int txtOff = palette.txtOff();
        int shown = Math.min(this.nearbyPlayers.size(), 8);
        int panelH = ((26 + (shown * 30)) + 6);
        int x = 6;
        int y = Math.max(6, ((screenH - panelH) - 126));
        fillRoundedRect(x, y, 230, panelH, 9.0f, bg);
        ClickGuiTheme.drawBorder(ctx, x, y, 230, panelH, palette.panelBorder());
        fillRect(ctx, (x + 8), (y + 22), ((x + 230) - 8), ((y + 22) + 1), rowBorder);
        String title = "NEARBY PLAYERS";
        String count = ("[" + this.nearbyPlayers.size() + "]");
        int countW = textWidth(tr, count);
        drawText(ctx, tr, title, (x + 8), (y + 7), txtOn);
        drawText(ctx, tr, count, (((x + 230) - countW) - 8), (y + 7), txtOff);
        int ty = ((y + 22) + 3);
        int i = 0;
        while (i < shown) {
            PlayerESP.NearbyPlayer np = ((PlayerESP.NearbyPlayer) this.nearbyPlayers.get(i));
            int rowX = (x + 5);
            int rowY = ty;
            int rowW = 220;
            fillRoundedRect(rowX, rowY, rowW, 30, 7.0f, rowBg);
            ClickGuiTheme.drawBorder(ctx, rowX, rowY, rowW, 30, ClickGuiTheme.withAlpha(rowBorder, 185));
            int friendColor = blend(accentSoft, accent, 0.48f);
            var tmp0 = new Object[1];
            tmp0[0] = np.dist;
            String dist = String.format("%.0fm", tmp0);
            int distW = textWidth(tr, dist);
            if (!(!np.friend)) {
                break label_40;
            }
            int nameColor = (!(!np.invisible) ? txtOff : txtOn);
            int nameMaxW = (((rowW - 12) - distW) - 8);
            drawText(ctx, tr, trimToWidth(tr, np.name, nameMaxW), (rowX + 6), (rowY + 4), nameColor);
            drawText(ctx, tr, dist, (((rowX + rowW) - distW) - 6), (rowY + 4), txtOff);
            String held = trimToWidth(tr, np.held, (rowW - 126));
            String stats = ("HP " + Math.round(np.hp) + "  A:" + np.armor);
            if (np.ping >= 0) {
                stats = (stats + "  P:" + np.ping);
            }
            if (np.ping >= 0) {
            } else {
            }
            int statsColor = txtOff;
            int statsW = textWidth(tr, stats);
            drawText(ctx, tr, held, (rowX + 6), (rowY + 14), txtOff);
            drawText(ctx, tr, stats, (((rowX + rowW) - statsW) - 6), (rowY + 14), statsColor);
            if (np.maxHp > 0.0f) {
            } else {
            }
            float hpPct = 0.0f;
            int barX = (rowX + 6);
            int barY = (rowY + 24);
            int barW = (rowW - 12);
            int hpFill = healthBarColor(hpPct, accent, accentSoft, rowBorder);
            fillRect(ctx, barX, barY, (barX + barW), (barY + 3), ClickGuiTheme.withAlpha(tglOff, 170));
            fillRect(ctx, barX, barY, (barX + Math.max(0, Math.min(barW, Math.round((((float) barW) * hpPct))))), (barY + 3), hpFill);
            ty += 30;
            i++;
        }
    }

    private static String trimToWidth(class_327 tr, String text, int maxWidth) {
        return TessellatorRenderer.trimToWidth(tr, text, maxWidth);
    }

    private static int textWidth(class_327 tr, String text) {
        float w = ((float) TessellatorRenderer.measureTextWidth(tr, text));
        if (w > 0.0f) return Math.round(w);
        Objects.requireNonNull(tr);
        return Math.max(0, ((int) Math.ceil(((double) (((float) text.length()) * Math.max(6.0f, (9.0f * 0.58f)))))));
    }

    private static void drawText(class_332 ctx, class_327 tr, String text, int x, int y, int color) {
        TessellatorRenderer.drawTextWithShadow(ctx, tr, text, x, y, color);
    }

    private static void fillRect(class_332 ctx, int x1, int y1, int x2, int y2, int color) {
        if ((x2 <= x1) || (y2 <= y1)) {
            return;
        }
        TessellatorRenderer.fillRect(ctx, x1, y1, x2, y2, color);
    }

    private static void fillRoundedRect(int x, int y, int w, int h, float radius, int color) {
        if ((w <= 0) || (h <= 0)) {
            return;
        }
        TessellatorRenderer.fillRoundedRect(x, y, w, h, radius, color);
    }

    private static int pingColor(int ping, int accent, int accentSoft, int txtOff) {
        if (ping < 50) return blend(accentSoft, accent, 0.25f);
        if (ping < 150) return blend(txtOff, accentSoft, 0.42f);
        return blend(txtOff, accent, 0.22f);
    }

    private static int healthBarColor(float hpPct, int accent, int accentSoft, int rowBorder) {
        if (hpPct > 0.6f) return blend(accent, accentSoft, 0.22f);
        if (hpPct > 0.3f) return blend(accent, rowBorder, 0.34f);
        return blend(rowBorder, accent, 0.18f);
    }

    private static int blend(int a, int b, float t) {
        float f = Math.max(0.0f, Math.min(1.0f, t));
        int aa = ((a >>> 24) & 255);
        int ar = ((a >>> 16) & 255);
        int ag = ((a >>> 8) & 255);
        int ab = (a & 255);
        int ba = ((b >>> 24) & 255);
        int br = ((b >>> 16) & 255);
        int bg = ((b >>> 8) & 255);
        int bb = (b & 255);
        int oa = ((int) (((float) aa) + (((float) (ba - aa)) * f)));
        int or = ((int) (((float) ar) + (((float) (br - ar)) * f)));
        int og = ((int) (((float) ag) + (((float) (bg - ag)) * f)));
        int ob = ((int) (((float) ab) + (((float) (bb - ab)) * f)));
        return ((((oa << 24) | (or << 16)) | (og << 8)) | ob);
    }

    private int resolveColor(double distSq, int entityId) {
        if (distSq > 4096.0) return RainbowUtil.distanceColor(distSq, 16384.0, -4160260, -48026);
        return RainbowUtil.rainbow(1.2, 0.85, ((long) entityId), 255);
    }

    private boolean isFriend(String name) {
        return VieCheatsMod.getInstance().getFriendManager().isFriend(name);
    }


    record NearbyPlayer(String name, double dist, float hp, float maxHp, int armor, int ping, String held, boolean friend, boolean invisible) {

        private NearbyPlayer(String name, double dist, float hp, float maxHp, int armor, int ping, String held, boolean friend, boolean invisible) {
            this.name = name;
            this.dist = dist;
            this.hp = hp;
            this.maxHp = maxHp;
            this.armor = armor;
            this.ping = ping;
            this.held = held;
            this.friend = friend;
            this.invisible = invisible;
        }

        public String name() {
            return this.name;
        }

        public double dist() {
            return this.dist;
        }

        public float hp() {
            return this.hp;
        }

        public float maxHp() {
            return this.maxHp;
        }

        public int armor() {
            return this.armor;
        }

        public int ping() {
            return this.ping;
        }

        public String held() {
            return this.held;
        }

        public boolean friend() {
            return this.friend;
        }

        public boolean invisible() {
            return this.invisible;
        }

    }
}
