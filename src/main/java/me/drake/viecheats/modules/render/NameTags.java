package me.drake.viecheats.modules.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.RenderEvent;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.ColorSetting;
import me.drake.viecheats.core.setting.DoubleSetting;
import me.drake.viecheats.modules.client.FakeRoles;
import me.drake.viecheats.modules.client.NameProtect;
import me.drake.viecheats.modules.exploit.Freecam;
import me.drake.viecheats.render.ExternalGlTextRenderer;
import me.drake.viecheats.render.TessellatorRenderer;
import me.drake.viecheats.render.VieMeshRenderer;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_243;
import net.minecraft.class_2583;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4184;
import net.minecraft.class_640;
import net.minecraft.class_7923;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class NameTags extends Module {
    private final BoolSetting selfNametag;
    private final BoolSetting ignoreSelf;
    private final BoolSetting ignoreFriends;
    private final BoolSetting ignoreBots;
    private final DoubleSetting scale;
    private final BoolSetting displayItems;
    private final BoolSetting ignoreEmpty;
    private final DoubleSetting itemSpacing;
    private final ColorSetting background;
    private final ColorSetting nameColor;
    private final ColorSetting distanceColor;
    private static final int RED = -59111;
    private static final int AMBER = -38631;
    private static final int GREEN = -15074279;
    private static final float ITEM_ICON_BASE_SCALE = 1.0f;
    private static final float DISTANCE_SCALE_NEAR = 6.0f;
    private static final float DISTANCE_SCALE_FAR = 56.0f;
    private static final float DISTANCE_SCALE_MIN = 0.55f;
    private final List playerList;
    private final NameTags.ImmediateSdfBridge immediateSDF;
    private final NameTags.DeferredItemBridge deferred;

    public NameTags() {
        super("NameTags", "Player nametags with health, distance, and equipment.", Category.RENDER);
        this.selfNametag = ((BoolSetting) addSetting(new BoolSetting("SelfNametag", "Render your own nametag.", true)));
        this.ignoreSelf = ((BoolSetting) addSetting(new BoolSetting("IgnoreSelf", "Hide self nametag in first-person when freecam is off.", false)));
        this.ignoreFriends = ((BoolSetting) addSetting(new BoolSetting("IgnoreFriends", "Ignore rendering nametags for friends.", false)));
        this.ignoreBots = ((BoolSetting) addSetting(new BoolSetting("IgnoreBots", "Only render non-bot player nametags.", true)));
        this.scale = ((DoubleSetting) addSetting(new DoubleSetting("Scale", "The scale of the nametag.", 1.1, 0.25, 4.0, 0.05)));
        this.displayItems = ((BoolSetting) addSetting(new BoolSetting("PlayerItems", "Displays armor and hand items above player nametags.", true)));
        this.ignoreEmpty = ((BoolSetting) addSetting(new BoolSetting("IgnoreEmptySlots", "Don't reserve spacing for empty player slots.", true)));
        this.itemSpacing = ((DoubleSetting) addSetting(new DoubleSetting("ItemSpacing", "Spacing between rendered player items.", 2.0, 0.0, 12.0, 0.25)));
        this.background = ((ColorSetting) addSetting(new ColorSetting("BackgroundColor", "Nametag background color.", -791029287)));
        this.nameColor = ((ColorSetting) addSetting(new ColorSetting("NameColor", "Name color.", -15658735)));
        this.distanceColor = ((ColorSetting) addSetting(new ColorSetting("DistanceColor", "Distance color.", -9803158)));
        this.playerList = new ArrayList();
        this.immediateSDF = new NameTags.ImmediateSdfBridge();
        this.deferred = new NameTags.DeferredItemBridge();
    }

    public String getInfo() {
        return Integer.toString(this.playerList.size());
    }

    public void onDisable() {
        super.onDisable();
        this.playerList.clear();
        this.deferred.clear();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.POST) return;
        if (!(isInGame())) {
            this.playerList.clear();
            this.deferred.clear();
            return;
        }
        this.playerList.clear();
        boolean freecamActive = isFreecamActive();
        boolean firstPerson = mc.field_1690.method_31044().method_31034();
        class_243 cameraPos = mc.field_1773.method_19418().method_71156();
        Object var5 = mc.field_1687.method_18456().iterator();
        while (var5.hasNext()) {
            class_1657 player = ((class_1657) var5.next());
            if (player == null) break label_10;
            if (!(player.method_5805())) {
                break label_10;
            }
            if (player == mc.field_1724) {
                if (!(((Boolean) this.selfNametag.get()).booleanValue())) {
                    break label_10;
                }
                if (!(shouldIgnoreSelfNametag(freecamActive, firstPerson))) break label_17;
                break label_10;
            }
            if (((Boolean) this.ignoreBots.get()).booleanValue() && isBot(player)) {
                break label_10;
            }
            if ((!(((Boolean) this.ignoreFriends.get()).booleanValue())) || (!(isFriend(player.method_5477().getString())))) break label_17;
            break label_10;
            label_17:
            this.playerList.add(player);
        }
        appendSelfIfNeeded(freecamActive, firstPerson);
        this.playerList.sort(Comparator.comparingDouble((player) -> player.method_5707(cameraPos)));
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (event.getStage() != RenderEvent.Stage.HUD_2D) return;
        if (!(isInGame())) return;
        if (this.playerList.isEmpty()) return;
        class_332 ctx = event.getDrawContext();
        if (ctx == null) return;
        if (!(TessellatorRenderer.isInFrame())) return;
        class_4184 camera = mc.field_1773.method_19418();
        if (camera == null) return;
        float tickDelta = event.getTickDelta();
        class_243 camPos = camera.method_71156();
        Matrix4f mvp = buildMvp(camera);
        this.deferred.clear();
        int i = (this.playerList.size() - 1);
        while (i >= 0) {
            class_1657 player = ((class_1657) this.playerList.get(i));
            if (player == null) break label_25;
            if (!(player.method_5805())) {
                break label_25;
            }
            class_243 playerPos = player.method_30950(tickDelta);
            class_243 worldPos = playerPos.method_1031(0.0, getTagHeight(player), 0.0);
            float[] screen = projectToScreen(worldPos.field_1352, worldPos.field_1351, worldPos.field_1350, mvp, camPos);
            if (screen == null) {
                break label_25;
            }
            float sx = screen[0];
            float sy = screen[1];
            float baseScale = ((Double) this.scale.get()).floatValue();
            double cameraDistance = camPos.method_1022(playerPos);
            renderNametagPlayer(ctx, player, sx, sy, baseScale, cameraDistance);
            label_25:
            i--;
        }
    }

    private void renderNametagPlayer(class_332 ctx, class_1657 player, float sx, float sy, float baseScale, double cameraDistance) {
        float distanceScaleMul = distanceScaleMultiplier(cameraDistance);
        float tagScale = (baseScale * distanceScaleMul);
        float mainSize = this.immediateSDF.bigSize(tagScale);
        NameTags.NameRenderParts nameParts = resolveNameParts(player);
        String prefixText = nameParts.prefixText();
        String nameText = nameParts.nameText();
        float absorption = player.method_6067();
        int health = Math.round((player.method_6032() + absorption));
        float maxHealth = Math.max(1.0f, (player.method_6063() + absorption));
        double healthPct = ((double) (((float) health) / maxHealth));
        String healthText = (" " + health);
        if (healthPct <= 0.333) {
            break label_14;
        }
        int healthColor = (healthPct <= 0.666 ? -38631 : -15074279);
        if (mc.field_1724 != null) {
        } else {
        }
        double distance = 0.0;
        distance = (((double) Math.round((distance * 10.0))) / 10.0);
        String distText = (" " + distance + "m");
        double nameW = (this.immediateSDF.getWidth(prefixText, mainSize) + this.immediateSDF.getWidth(nameText, mainSize));
        double healthW = this.immediateSDF.getWidth(healthText, mainSize);
        double distW = this.immediateSDF.getWidth(distText, mainSize);
        double h = this.immediateSDF.getHeight(mainSize);
        double width = ((nameW + healthW) + distW);
        double x = (((double) sx) - (width / 2.0));
        double y = (((double) sy) - h);
        drawBg(ctx, x, y, width, h);
        this.immediateSDF.beginBig(tagScale);
        double hx = x;
        if (!(prefixText.isEmpty())) {
            hx = this.immediateSDF.render(ctx, prefixText, hx, y, nameParts.prefixColor(), false);
        }
        hx = this.immediateSDF.render(ctx, nameText, hx, y, nameParts.nameColor(), false);
        hx = this.immediateSDF.render(ctx, healthText, hx, y, healthColor, false);
        this.immediateSDF.render(ctx, distText, hx, y, ((Integer) this.distanceColor.get()).intValue(), false);
        this.immediateSDF.end();
        if (((Boolean) this.displayItems.get()).booleanValue()) {
            renderPlayerItems(ctx, player, sx, y, tagScale, distanceScaleMul);
        }
    }

    private NameTags.NameRenderParts resolveNameParts(class_1657 player) {
        String protectedName = resolveProtectedName(player);
        if ((player != mc.field_1724) || (!(FakeRoles.isActive()))) break label_8;
        String prefix = FakeRoles.getEffectivePrefix();
        if (prefix != null) {
            prefix = prefix.trim();
        }
        if ((prefix == null) || (prefix.isEmpty())) break label_8;
        int prefixColor = colorFromStyle(FakeRoles.getRolePrefixStyle(), ((Integer) this.nameColor.get()).intValue());
        int fakeNameColor = colorFromStyle(FakeRoles.getRoleNameStyle(), ((Integer) this.nameColor.get()).intValue());
        return new NameTags.NameRenderParts(prefix, protectedName, prefixColor, fakeNameColor);
        label_8:
        return new NameTags.NameRenderParts("", protectedName, ((Integer) this.nameColor.get()).intValue(), ((Integer) this.nameColor.get()).intValue());
    }

    private String resolveProtectedName(class_1657 player) {
        String rawName = null;
        if (player != null && player.method_7334() != null) {
            rawName = player.method_7334().name();
        }
        if (rawName != null) {
            if (rawName.isBlank()) {
            }
            if (player == null) {
            } else {
            }
            rawName = player.method_5477().getString();
        }
        if (player == mc.field_1724) {
            rawName = NameProtect.staticMaskSelfName(rawName);
        }
        String replaced = NameProtect.staticReplace(rawName);
        return (replaced == null ? rawName : replaced);
    }

    private void renderPlayerItems(class_332 ctx, class_1657 player, float sx, double textY, float tagScale, float distanceScaleMul) {
        class_1799[] all = getPlayerDisplayItems(player);
        List renderStacks = new ArrayList(all.length);
        List renderIndices = new ArrayList(all.length);
        int slotIndex = 0;
        while (slotIndex < all.length) {
            class_1799 stack = all[slotIndex];
            if (((Boolean) this.ignoreEmpty.get()).booleanValue() && stack.method_7960()) {
            } else {
                renderStacks.add(stack);
                renderIndices.add(slotIndex);
            }
            slotIndex++;
        }
        if (renderStacks.isEmpty()) return;
        float iconScale = Math.max(0.6f, (1.0f * distanceScaleMul));
        int iconSize = Math.max(10, Math.round((16.0f * iconScale)));
        double step = (((double) iconSize) + ((Double) this.itemSpacing.get()).doubleValue());
        double totalW = ((step * ((double) renderStacks.size())) - ((Double) this.itemSpacing.get()).doubleValue());
        double startX = (((double) sx) - (totalW / 2.0));
        double y = ((textY - 7.0) - ((double) iconSize));
        List slotX = new ArrayList(renderStacks.size());
        int i = 0;
        while (i < renderStacks.size()) {
            double x = (startX + (((double) i) * step));
            slotX.add(x);
            this.deferred.queueItem(((class_1799) renderStacks.get(i)), x, y, iconScale, true, null);
            i++;
        }
        this.deferred.flush(ctx, mc.field_1772);
        float durabilitySize = this.immediateSDF.smallScaledSize(0.4, tagScale);
        i = 0;
        while (i < renderStacks.size()) {
            slotIndex = ((Integer) renderIndices.get(i)).intValue();
            stack = ((class_1799) renderStacks.get(i));
            if (!(shouldRenderDurabilityPercent(slotIndex, stack))) {
            } else {
                String percentText = durabilityPercentText(stack);
                double textW = this.immediateSDF.getWidth(percentText, durabilitySize);
                x = (((((Double) slotX.get(i)).doubleValue() + ((double) iconSize)) - textW) - 1.0);
                double yText = ((y + ((double) iconSize)) + 1.0);
                int color = withAlpha(stack.method_31580(), 255);
                this.immediateSDF.render(ctx, percentText, x, yText, color, false, durabilitySize);
            }
            i++;
        }
    }

    private float distanceScaleMultiplier(double cameraDistance) {
        if (!(Double.isFinite(cameraDistance))) return 1.0f;
        if (cameraDistance <= 6.0) return 1.0f;
        if (cameraDistance >= 56.0) return 0.55f;
        float t = ((float) ((cameraDistance - 6.0) / 50.0));
        t = Math.max(0.0f, Math.min(1.0f, t));
        t = ((t * t) * (3.0f - (2.0f * t)));
        return (1.0f + (-0.45f * t));
    }

    private class_1799[] getPlayerDisplayItems(class_1657 player) {
        var tmp0 = new class_1799[6];
        tmp0[0] = player.method_6047();
        tmp0[1] = player.method_6118(class_1304.field_6169);
        tmp0[2] = player.method_6118(class_1304.field_6174);
        tmp0[3] = player.method_6118(class_1304.field_6172);
        tmp0[4] = player.method_6118(class_1304.field_6166);
        tmp0[5] = player.method_6079();
        return tmp0;
    }

    private boolean shouldRenderDurabilityPercent(int slotIndex, class_1799 stack) {
        if (((stack == null) || (stack.method_7960())) || (!(stack.method_7963()))) {
            return false;
        }
        if (slotIndex >= 1 && slotIndex <= 4) return true;
        if (slotIndex != 0) {
            if (slotIndex != 5) break label_5;
        }
        return isToolLike(stack);
    }

    private boolean isToolLike(class_1799 stack) {
        String path = class_7923.field_41178.method_10221(stack.method_7909()).method_12832();
        if (!(path.endsWith("_pickaxe")) && !(path.endsWith("_axe")) && !(path.endsWith("_shovel")) && !(path.endsWith("_hoe")) && !(path.endsWith("_sword")) && !(path.endsWith("_trident")) && !(path.endsWith("_mace")) && !(path.equals("shears")) && !(path.equals("fishing_rod")) && !(path.equals("flint_and_steel")) && !(path.equals("brush")) && !(path.equals("bow"))) {
            if (path.equals("crossbow")) {
            }
        } else {
        }
        return false;
    }

    private String durabilityPercentText(class_1799 stack) {
        int max = Math.max(1, stack.method_7936());
        int remaining = Math.max(0, (max - stack.method_7919()));
        int percent = Math.round(((((float) remaining) * 100.0f) / ((float) max)));
        var tmp0 = new Object[1];
        tmp0[0] = percent;
        return String.format(Locale.ROOT, "%d%%", tmp0);
    }

    private boolean isBot(class_1657 player) {
        if (player == mc.field_1724) return false;
        if (mc.method_1562() == null) return true;
        class_640 entry = mc.method_1562().method_2871(player.method_5667());
        return entry == null;
    }

    private boolean isFriend(String name) {
        VieCheatsMod instance = VieCheatsMod.getInstance();
        if ((instance == null) || (instance.getFriendManager() == null)) break label_2;
        return instance.getFriendManager().isFriend(name);
    }

    private boolean shouldIgnoreSelfNametag(boolean freecamActive, boolean firstPerson) {
        if ((!(((Boolean) this.ignoreSelf.get()).booleanValue())) || (freecamActive)) break label_1;
        return !(!firstPerson);
    }

    private void appendSelfIfNeeded(boolean freecamActive, boolean firstPerson) {
        if ((mc.field_1724 == null) || (!(mc.field_1724.method_5805()))) {
            return;
        }
        if (!(((Boolean) this.selfNametag.get()).booleanValue())) return;
        if (shouldIgnoreSelfNametag(freecamActive, firstPerson)) return;
        if (!(this.playerList.contains(mc.field_1724))) {
            this.playerList.add(mc.field_1724);
        }
    }

    private boolean isFreecamActive() {
        if (VieCheatsMod.getInstance() == null) {
        } else {
        }
        Freecam freecam = ((Freecam) VieCheatsMod.getInstance().getModuleManager().getModule(Freecam.class));
        if (freecam == null) break label_4;
        return freecam.isEnabled();
    }

    private double getTagHeight(class_1297 entity) {
        return (((double) entity.method_18381(entity.method_18376())) + 0.5);
    }

    private void drawBg(class_332 ctx, double x, double y, double width, double height) {
        this.immediateSDF.drawPanel(ctx, (x - 1.0), (y - 1.0), (width + 2.0), (height + 2.0), ((Integer) this.background.get()).intValue());
    }

    private static int withAlpha(int rgb, int alpha) {
        return (((alpha & 255) << 24) | (rgb & 16777215));
    }

    private static int colorFromStyle(class_2583 style, int fallbackArgb) {
        if ((style == null) || (style.method_10973() == null)) {
            return fallbackArgb;
        }
        return withAlpha(style.method_10973().method_27716(), 255);
    }

    private static Matrix4f buildMvp(class_4184 camera) {
        Quaternionf conjugate = camera.method_23767().conjugate(new Quaternionf());
        Matrix4f viewMatrix = new Matrix4f().rotation(conjugate);
        return new Matrix4f(VieMeshRenderer.projection).mul(viewMatrix);
    }

    private float[] projectToScreen(double wx, double wy, double wz, Matrix4f mvp, class_243 camPos) {
        float rx = ((float) (wx - camPos.field_1352));
        float ry = ((float) (wy - camPos.field_1351));
        float rz = ((float) (wz - camPos.field_1350));
        Vector4f pos = new Vector4f(rx, ry, rz, 1.0f);
        mvp.transform(pos);
        if (pos.w <= 0.0f) return null;
        float ndcX = (pos.x / pos.w);
        float ndcY = (pos.y / pos.w);
        int w = mc.method_22683().method_4486();
        int h = mc.method_22683().method_4502();
        float screenX = (((ndcX + 1.0f) * 0.5f) * ((float) w));
        float screenY = (((1.0f - ndcY) * 0.5f) * ((float) h));
        var tmp0 = new float[2];
        tmp0[0] = screenX;
        tmp0[1] = screenY;
        return tmp0;
    }


    record DeferredItemCall(class_1799 stack, double x, double y, float scale, boolean overlay, String countOverride) {

        private DeferredItemCall(class_1799 stack, double x, double y, float scale, boolean overlay, String countOverride) {
            this.stack = stack;
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.overlay = overlay;
            this.countOverride = countOverride;
        }

        public class_1799 stack() {
            return this.stack;
        }

        public double x() {
            return this.x;
        }

        public double y() {
            return this.y;
        }

        public float scale() {
            return this.scale;
        }

        public boolean overlay() {
            return this.overlay;
        }

        public String countOverride() {
            return this.countOverride;
        }

    }

    final class ImmediateSdfBridge {
        private static final float BIG_SIZE = 11.0f;
        private float currentSize;

        private ImmediateSdfBridge() {
            this.currentSize = 11.0f;
        }

        float bigSize(float tagScale) {
            return snapSize((11.0f * Math.max(0.01f, tagScale)));
        }

        float smallScaledSize(double mul, float tagScale) {
            return snapSize((11.0f * ((float) Math.max(0.01, (mul * ((double) tagScale))))));
        }

        void beginBig(float tagScale) {
            this.currentSize = bigSize(tagScale);
        }

        double getWidth(String text, float sizePx) {
            if ((text == null) || (text.isEmpty())) {
                return 0.0;
            }
            float snapped = snapSize(sizePx);
            int width = ExternalGlTextRenderer.measureTextWidth(text, snapped);
            if (width > 0) return ((double) width);
            if (NameTags.access$000() != null && NameTags.access$100().field_1772 != null) {
                Objects.requireNonNull(NameTags.access$200().field_1772);
                float ratio = (snapped / Math.max(1.0f, 9.0f));
                return ((double) (((float) NameTags.access$300().field_1772.method_1727(text)) * ratio));
            }
            return ((double) (((float) text.length()) * Math.max(4.0f, (snapped * 0.5f))));
        }

        double getHeight(float sizePx) {
            float snapped = snapSize(sizePx);
            int h = ExternalGlTextRenderer.lineHeight(snapped);
            if (h > 0) return ((double) h);
            if (NameTags.access$400() != null && NameTags.access$500().field_1772 != null) {
                Objects.requireNonNull(NameTags.access$600().field_1772);
                float ratio = (snapped / Math.max(1.0f, 9.0f));
                Objects.requireNonNull(NameTags.access$700().field_1772);
                return ((double) (9.0f * ratio));
            }
            return ((double) Math.max(6.0f, snapped));
        }

        double render(class_332 ctx, String text, double x, double y, int color, boolean shadow) {
            return render(ctx, text, x, y, color, shadow, this.currentSize);
        }

        double render(class_332 ctx, String text, double x, double y, int color, boolean shadow, float sizePx) {
            if (((ctx == null) || (text == null)) || (text.isEmpty())) {
                return x;
            }
            float snapped = snapSize(sizePx);
            ExternalGlTextRenderer.drawText(ctx, text, ((int) Math.round(x)), ((int) Math.round(y)), color, shadow, snapped);
            return (x + getWidth(text, snapped));
        }

        void end() {
        }

        void drawPanel(class_332 ctx, double x, double y, double width, double height, int color) {
            if (ctx == null) return;
            int x1 = ((int) Math.floor(x));
            int y1 = ((int) Math.floor(y));
            int x2 = ((int) Math.ceil((x + width)));
            int y2 = ((int) Math.ceil((y + height)));
            int w = Math.max(1, (x2 - x1));
            int h = Math.max(1, (y2 - y1));
            float radius = Math.max(1.0f, Math.min((((float) Math.min(w, h)) * 0.28f), 4.0f));
            TessellatorRenderer.fillRoundedRect(ctx, x1, y1, w, h, radius, color);
        }

        private float snapSize(float sizePx) {
            return Math.max(1.0f, ((float) Math.round(sizePx)));
        }

    }

    record NameRenderParts(String prefix, String name, int prefixColor, int nameColor) {

        private NameRenderParts(String prefix, String name, int prefixColor, int nameColor) {
            this.prefix = prefix;
            this.name = name;
            this.prefixColor = prefixColor;
            this.nameColor = nameColor;
        }

        String prefixText() {
            if ((this.prefix == null) || (this.prefix.isBlank())) {
                return "";
            }
            if ((this.name == null) || (this.name.isBlank())) {
                return this.prefix;
            }
            return (this.prefix + " ");
        }

        String nameText() {
            return (this.name == null ? "" : this.name);
        }

        public String prefix() {
            return this.prefix;
        }

        public String name() {
            return this.name;
        }

        public int prefixColor() {
            return this.prefixColor;
        }

        public int nameColor() {
            return this.nameColor;
        }

    }

    final class DeferredItemBridge {
        private final List calls;

        private DeferredItemBridge() {
            this.calls = new ArrayList();
        }

        void queueItem(class_1799 stack, double x, double y, float scale, boolean overlay, String countOverride) {
            if ((stack == null) || (stack.method_7960())) {
                return;
            }
            this.calls.add(new NameTags.DeferredItemCall(stack.method_7972(), x, y, scale, overlay, countOverride));
        }

        void flush(class_332 ctx, class_327 textRenderer) {
            if (ctx == null) break label_1;
            if (this.calls.isEmpty()) {
                label_1:
                return;
            }
            Object var3 = this.calls.iterator();
            while (var3.hasNext()) {
                NameTags.DeferredItemCall call = ((NameTags.DeferredItemCall) var3.next());
                ctx.method_51448().pushMatrix();
                ctx.method_51448().translate(((float) call.x), ((float) call.y));
                ctx.method_51448().scale(call.scale, call.scale);
                ctx.method_51427(call.stack, 0, 0);
                if (!(!call.overlay)) {
                    ctx.method_51432(textRenderer, call.stack, 0, 0, call.countOverride);
                }
                ctx.method_51448().popMatrix();
            }
            ctx.method_73199();
            this.calls.clear();
        }

        void clear() {
            this.calls.clear();
        }

    }
}
