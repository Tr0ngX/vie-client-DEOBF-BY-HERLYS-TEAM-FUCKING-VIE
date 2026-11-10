package me.drake.viecheats.gui;

import java.util.ArrayList;
import java.util.List;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.modules.client.ClientSettings;
import me.drake.viecheats.modules.donut.RegionMap;
import me.drake.viecheats.modules.render.SpotiPlayRenderer;
import me.drake.viecheats.modules.render.SpotifyHUD;
import me.drake.viecheats.render.TessellatorRenderer;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class HUDEditorScreen extends class_437 {
    private SpotifyHUD spotify;
    private RegionMap regionMap;
    private final List targets;
    private HUDEditorScreen.HudTarget hoveredTarget;
    private HUDEditorScreen.TargetId draggingTargetId;
    private int dragOffsetX;
    private int dragOffsetY;
    private int dragStartMouseX;
    private int dragStartMouseY;
    private int dragStartSettingX;
    private int dragStartSettingY;
    private int dragTargetW;
    private int dragTargetH;
    private boolean typingCoords;
    private HUDEditorScreen.TargetId typingTargetId;
    private String typingBuffer;

    public HUDEditorScreen() {
        super(class_2561.method_43470("HUD Editor"));
        this.targets = new ArrayList();
        this.typingBuffer = "";
    }

    protected void method_25426() {
        SpotiPlayRenderer.setEditorActive(true);
        VieCheatsMod mod = VieCheatsMod.getInstance();
        if (mod != null && mod.getModuleManager() != null) {
            this.spotify = ((SpotifyHUD) mod.getModuleManager().getModule(SpotifyHUD.class));
            this.regionMap = ((RegionMap) mod.getModuleManager().getModule(RegionMap.class));
        }
        refreshTargets();
    }

    public void method_25394(class_332 ctx, int mx, int my, float delta) {
        super.method_25394(ctx, mx, my, delta);
        TessellatorRenderer.setContext(ctx);
        refreshTargets();
        this.hoveredTarget = findTargetAt(mx, my);
        ctx.method_25294(0, 0, this.field_22789, this.field_22790, 2047938833);
        ctx.method_27534(this.field_22793, class_2561.method_43470("HUD Editor | Drag HUD to move | T: input X,Y | Enter: apply | ESC: close"), (this.field_22789 / 2), 8, -1644826);
        Object var5 = this.targets.iterator();
        while (var5.hasNext()) {
            HUDEditorScreen.HudTarget target = ((HUDEditorScreen.HudTarget) var5.next());
            if (this.hoveredTarget == null) break label_11;
            boolean hover = this.hoveredTarget.id() == target.id();
            boolean dragging = this.draggingTargetId == target.id();
            if (!(!dragging)) {
                break label_19;
            }
            int fill = (!(!hover) ? 542759423 : 305147952);
            if (!(!dragging)) {
                break label_23;
            }
            int border = (!(!hover) ? -11544065 : -5197648);
            TessellatorRenderer.fillRoundedRect(ctx, target.x(), target.y(), target.w(), target.h(), 6, fill);
            TessellatorRenderer.strokeRoundedRect(ctx, target.x(), target.y(), target.w(), target.h(), 6.0f, 1.5f, border);
            String title = (target.label() + " [" + target.x() + ", " + target.y() + "]");
            int labelY = Math.max(20, (target.y() - 10));
            TessellatorRenderer.drawTextWithShadow(ctx, this.field_22793, title, (target.x() + 4), labelY, border);
            if (target.id() == HUDEditorScreen.TargetId.SPOTIFY) {
                drawSpotifyPreview(ctx, target);
            }
        }
        if (!(!this.typingCoords) && this.typingTargetId != null) {
            drawTypingPanel(ctx);
        }
    }

    private void drawTypingPanel(class_332 ctx) {
        switch (this.typingTargetId.ordinal()) {
            case 0 -> { break label_2; }
            case 1 -> { break label_3; }
            case 2 -> { break label_4; }
            case 3 -> { break label_5; }
            case 4 -> { break label_6; }
            case 5 -> { break label_7; }
            default -> { break label_1; }
        }
        label_1:
        throw new MatchException(null, null);
        label_2:
        break label_8;
        label_3:
        break label_8;
        label_4:
        break label_8;
        label_5:
        break label_8;
        label_6:
        break label_8;
        label_7:
        label_8:
        String label = "Hotkeys";
        int panelW = Math.max(220, (TessellatorRenderer.measureTextWidth(this.field_22793, this.typingBuffer) + 48));
        int panelH = 42;
        int x = ((this.field_22789 - panelW) / 2);
        int y = ((this.field_22790 - panelH) - 14);
        TessellatorRenderer.fillRoundedRect(ctx, x, y, panelW, panelH, 7, -434364636);
        TessellatorRenderer.strokeRoundedRect(ctx, x, y, panelW, panelH, 7.0f, 1.4f, -11544065);
        TessellatorRenderer.drawTextWithShadow(ctx, this.field_22793, ("Input " + label + " (x,y):"), (x + 8), (y + 7), -1710619);
        TessellatorRenderer.drawTextWithShadow(ctx, this.field_22793, (this.typingBuffer + "_"), (x + 8), (y + 21), -8394241);
    }

    private void drawSpotifyPreview(class_332 ctx, HUDEditorScreen.HudTarget target) {
        int pad = Math.max(4, Math.round((((float) target.h()) * 0.1f)));
        int x = (target.x() + pad);
        int y = (target.y() + pad);
        int w = Math.max(20, (target.w() - (pad * 2)));
        int h = Math.max(16, (target.h() - (pad * 2)));
        int art = Math.max(14, (h - 6));
        TessellatorRenderer.fillRoundedRect(ctx, x, y, w, h, 5, -652862686);
        TessellatorRenderer.fillRoundedRect(ctx, (x + 3), (y + 3), art, art, 4, -1441720803);
        int infoX = ((x + 8) + art);
        int lineW = Math.max(12, ((w - art) - 14));
        TessellatorRenderer.fillRectSharp(ctx, infoX, (y + 5), lineW, 2, -867182081);
        TessellatorRenderer.fillRectSharp(ctx, infoX, (y + 10), Math.max(10, (lineW - 16)), 2, -1598505274);
    }

    public boolean method_25402(class_11909 click, boolean doubled) {
        int mx = ((int) click.comp_4798());
        int my = ((int) click.comp_4799());
        refreshTargets();
        this.hoveredTarget = findTargetAt(mx, my);
        if (!(click.method_74245()) && this.hoveredTarget != null) {
            this.typingCoords = false;
            this.draggingTargetId = this.hoveredTarget.id();
            this.dragTargetW = this.hoveredTarget.w();
            this.dragTargetH = this.hoveredTarget.h();
            if (this.hoveredTarget.absolute()) {
                this.dragOffsetX = (mx - this.hoveredTarget.x());
                this.dragOffsetY = (my - this.hoveredTarget.y());
            } else {
                this.dragStartMouseX = mx;
                this.dragStartMouseY = my;
                this.dragStartSettingX = getOffsetX(this.draggingTargetId);
                this.dragStartSettingY = getOffsetY(this.draggingTargetId);
            }
            return true;
        }
        return super.method_25402(click, doubled);
    }

    public boolean method_25403(class_11909 click, double dx, double dy) {
        if (click.method_74245()) break label_1;
        if (this.draggingTargetId == null) {
            label_1:
            return super.method_25403(click, dx, dy);
        }
        int mx = ((int) click.comp_4798());
        int my = ((int) click.comp_4799());
        HUDEditorScreen.HudTarget current = getTargetById(this.draggingTargetId);
        if (current == null) return true;
        if (current.absolute()) {
            int newX = clamp((mx - this.dragOffsetX), 0, Math.max(0, (this.field_22789 - this.dragTargetW)));
            int newY = clamp((my - this.dragOffsetY), 0, Math.max(0, (this.field_22790 - this.dragTargetH)));
            setAbsolutePosition(this.draggingTargetId, newX, newY);
        } else {
            int deltaX = (mx - this.dragStartMouseX);
            int deltaY = (my - this.dragStartMouseY);
            setOffsetPosition(this.draggingTargetId, (this.dragStartSettingX + deltaX), (this.dragStartSettingY + deltaY));
        }
        refreshTargets();
        return true;
    }

    public boolean method_25406(class_11909 click) {
        this.draggingTargetId = null;
        return super.method_25406(click);
    }

    public boolean method_25404(class_11908 input) {
        int key = input.comp_4795();
        if (!(!this.typingCoords)) {
            if (key == 257) break label_3;
            if (key == 335) {
                label_3:
                applyTypedPosition();
                this.typingCoords = false;
                this.typingTargetId = null;
                this.typingBuffer = "";
                return true;
            }
            if (key == 256) {
                this.typingCoords = false;
                this.typingTargetId = null;
                this.typingBuffer = "";
                return true;
            }
            if (key == 259) {
                if (!(this.typingBuffer.isEmpty())) {
                    this.typingBuffer = this.typingBuffer.substring(0, (this.typingBuffer.length() - 1));
                }
                return true;
            }
            if (input.method_74243()) {
                if ((this.field_22787 == null) || (this.field_22787.field_1774 == null)) break label_22;
                String clip = this.field_22787.field_1774.method_1460();
                if ((clip == null) || (clip.isEmpty())) break label_22;
                this.typingBuffer = (this.typingBuffer + clip.replace(10, 32).replace(13, 32));
                label_22:
                return true;
            }
            return true;
        }
        if (key != 84) break label_33;
        if (this.hoveredTarget != null) {
        } else {
        }
        HUDEditorScreen.HudTarget target = getTargetById(this.draggingTargetId);
        if (target == null) break label_33;
        this.typingCoords = true;
        this.typingTargetId = target.id();
        this.typingBuffer = (target.x() + "," + target.y());
        return true;
        label_33:
        if (input.method_74231()) {
            method_25419();
            return true;
        }
        return super.method_25404(input);
    }

    public boolean method_25400(class_11905 input) {
        if (!this.typingCoords) return super.method_25400(input);
        char c = ((char) input.comp_4793());
        if (c >= 32 && c < 127 && this.typingBuffer.length() < 48) {
            this.typingBuffer = (this.typingBuffer + c);
            return true;
        }
        return super.method_25400(input);
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
    }

    public void method_25419() {
        SpotiPlayRenderer.setEditorActive(false);
        requestConfigSave();
        super.method_25419();
    }

    public void method_25432() {
        SpotiPlayRenderer.setEditorActive(false);
        requestConfigSave();
        super.method_25432();
    }

    private void requestConfigSave() {
        VieCheatsMod mod = VieCheatsMod.getInstance();
        if (mod != null && mod.getConfigManager() != null) {
            mod.getConfigManager().requestSave();
        }
    }

    private void refreshTargets() {
        this.targets.clear();
        collectSpotifyTarget();
        collectRegionMapTarget();
        collectHudManagerTargets();
    }

    private void collectSpotifyTarget() {
        if ((this.spotify == null) || (!(this.spotify.isEnabled()))) {
            return;
        }
        float sc = this.spotify.getScale();
        int artSize = ((int) (54.0f * sc));
        int pad = ((int) (10.0f * sc));
        int gap = ((int) (12.0f * sc));
        int infoW = ((int) (175.0f * sc));
        int lineH = ((int) (9.0f * sc));
        int barH = ((int) (4.0f * sc));
        int timeGap = ((int) (5.0f * sc));
        int playSize = Math.max(7, Math.min(10, Math.round((7.2f * sc))));
        int textAreaH = ((((((lineH + timeGap) + barH) + timeGap) + lineH) + ((int) (8.0f * sc))) + playSize);
        int w = ((((pad + artSize) + gap) + infoW) + pad);
        int h = Math.max(((pad + artSize) + pad), ((pad + textAreaH) + pad));
        this.targets.add(new HUDEditorScreen.HudTarget(HUDEditorScreen.TargetId.SPOTIFY, "Spotify HUD", this.spotify.getPosX(), this.spotify.getPosY(), w, h, true));
    }

    private void collectRegionMapTarget() {
        if ((this.regionMap == null) || (!(this.regionMap.isEnabled()))) {
            return;
        }
        this.targets.add(new HUDEditorScreen.HudTarget(HUDEditorScreen.TargetId.REGION_MAP, "Region Map", this.regionMap.getPosX(), this.regionMap.getPosY(), this.regionMap.getHudWidth(), this.regionMap.getHudHeight(), true));
    }

    private void collectHudManagerTargets() {
        VieCheatsMod mod = VieCheatsMod.getInstance();
        if ((mod == null) || (mod.getHudManager() == null)) {
            return;
        }
        HudManager hud = mod.getHudManager();
        addHudBoundsTarget(HUDEditorScreen.TargetId.TOP_INFO, "Top Info", hud.getTopInfoBounds());
        addHudBoundsTarget(HUDEditorScreen.TargetId.COORDS, "Coordinates", hud.getCoordsBounds());
        addHudBoundsTarget(HUDEditorScreen.TargetId.MODULE_LIST, "Functions (Grouped)", hud.getModuleListBounds());
        addHudBoundsTarget(HUDEditorScreen.TargetId.HOTKEYS, "Hotkeys", hud.getHotkeysBounds());
    }

    private void addHudBoundsTarget(HUDEditorScreen.TargetId id, String label, HudManager.HudBounds bounds) {
        if ((bounds == null) || (!(bounds.visible()))) {
            return;
        }
        this.targets.add(new HUDEditorScreen.HudTarget(id, label, bounds.x(), bounds.y(), bounds.width(), bounds.height(), false));
    }

    private HUDEditorScreen.HudTarget findTargetAt(int mx, int my) {
        int i = (this.targets.size() - 1);
        while (i >= 0) {
            HUDEditorScreen.HudTarget target = ((HUDEditorScreen.HudTarget) this.targets.get(i));
            if (target.contains(mx, my)) return target;
            i--;
        }
        return null;
    }

    private HUDEditorScreen.HudTarget getTargetById(HUDEditorScreen.TargetId id) {
        if (id == null) return null;
        Object var2 = this.targets.iterator();
        while (var2.hasNext()) {
            HUDEditorScreen.HudTarget target = ((HUDEditorScreen.HudTarget) var2.next());
            if (target.id() == id) return target;
        }
        return null;
    }

    private void applyTypedPosition() {
        if (this.typingTargetId == null) return;
        int[] parsed = parsePosition(this.typingBuffer);
        if (parsed == null) return;
        int targetX = parsed[0];
        int targetY = parsed[1];
        HUDEditorScreen.HudTarget current = getTargetById(this.typingTargetId);
        if (current == null) return;
        if (current.absolute()) {
            int clampedX = clamp(targetX, 0, Math.max(0, (this.field_22789 - current.w())));
            int clampedY = clamp(targetY, 0, Math.max(0, (this.field_22790 - current.h())));
            setAbsolutePosition(this.typingTargetId, clampedX, clampedY);
        } else {
            int dx = (targetX - current.x());
            int dy = (targetY - current.y());
            setOffsetPosition(this.typingTargetId, (getOffsetX(this.typingTargetId) + dx), (getOffsetY(this.typingTargetId) + dy));
        }
        refreshTargets();
    }

    private int[] parsePosition(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        if (cleaned.isEmpty()) return null;
        String[] parts = cleaned.split("[,\\s]+");
        if (parts.length < 2) {
            return null;
            try {
            }
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());
            var tmp0 = new int[2];
            tmp0[0] = x;
            tmp0[1] = y;
            return tmp0;
        } catch (NumberFormatException ex9) {
            NumberFormatException ignored = ex9;
            return null;
        }
    }

    private int getOffsetX(HUDEditorScreen.TargetId id) {
        ClientSettings cs = ClientSettings.get();
        if ((cs == null) || (id == null)) {
            return 0;
        }
        switch (id.ordinal()) {
            case 2 -> { break label_4; }
            case 3 -> { break label_5; }
            case 4 -> { break label_6; }
            case 5 -> { break label_7; }
            default -> { break label_8; }
        }
        label_4:
        break label_9;
        label_5:
        break label_9;
        label_6:
        break label_9;
        label_7:
        break label_9;
        label_8:
        label_9:
        return 0;
    }

    private int getOffsetY(HUDEditorScreen.TargetId id) {
        ClientSettings cs = ClientSettings.get();
        if ((cs == null) || (id == null)) {
            return 0;
        }
        switch (id.ordinal()) {
            case 2 -> { break label_4; }
            case 3 -> { break label_5; }
            case 4 -> { break label_6; }
            case 5 -> { break label_7; }
            default -> { break label_8; }
        }
        label_4:
        break label_9;
        label_5:
        break label_9;
        label_6:
        break label_9;
        label_7:
        break label_9;
        label_8:
        label_9:
        return 0;
    }

    private void setOffsetPosition(HUDEditorScreen.TargetId id, int x, int y) {
        ClientSettings cs = ClientSettings.get();
        if ((cs == null) || (id == null)) {
            return;
        }
        switch (id.ordinal()) {
            case 2 -> { break label_4; }
            case 3 -> { break label_7; }
            case 4 -> { break label_10; }
            case 5 -> { break label_13; }
            default -> { break label_16; }
        }
        label_4:
        cs.setTopInfoOffsetX(x);
        cs.setTopInfoOffsetY(y);
        break label_16;
        label_7:
        cs.setCoordsOffsetX(x);
        cs.setCoordsOffsetY(y);
        break label_16;
        label_10:
        cs.setModuleListOffsetX(x);
        cs.setModuleListOffsetY(y);
        break label_16;
        label_13:
        cs.setHotkeysOffsetX(x);
        cs.setHotkeysOffsetY(y);
        break label_16;
        label_16:
    }

    private void setAbsolutePosition(HUDEditorScreen.TargetId id, int x, int y) {
        switch (id.ordinal()) {
            case 0 -> { break label_1; }
            case 1 -> { break label_4; }
            default -> { break label_7; }
        }
        label_1:
        if (this.spotify == null) break label_7;
        this.spotify.posX.set(x);
        this.spotify.posY.set(y);
        break label_7;
        label_4:
        if (this.regionMap == null) break label_7;
        this.regionMap.setPosX(x);
        this.regionMap.setPosY(y);
        break label_7;
        label_7:
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }


    record HudTarget(HUDEditorScreen.TargetId id, String label, int x, int y, int w, int h, boolean absolute) {

        private HudTarget(HUDEditorScreen.TargetId id, String label, int x, int y, int w, int h, boolean absolute) {
            this.id = id;
            this.label = label;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.absolute = absolute;
        }

        boolean contains(int mx, int my) {
            if (((mx < this.x) || (mx > (this.x + this.w))) || (my < this.y)) break label_1;
            return my <= (this.y + this.h);
        }

        public HUDEditorScreen.TargetId id() {
            return this.id;
        }

        public String label() {
            return this.label;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

        public int w() {
            return this.w;
        }

        public int h() {
            return this.h;
        }

        public boolean absolute() {
            return this.absolute;
        }

    }

    enum TargetId {
        SPOTIFY,
        REGION_MAP,
        TOP_INFO,
        COORDS,
        MODULE_LIST,
        HOTKEYS;


        private TargetId() {
        }

    }
}
