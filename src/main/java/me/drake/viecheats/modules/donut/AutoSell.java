package me.drake.viecheats.modules.donut;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.core.setting.StringSetting;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_465;
import net.minecraft.class_7923;

public class AutoSell extends Module {
    private final StringSetting targetItems;
    private final StringSetting sellCommand;
    private final IntSetting sellButtonSlot;
    private final IntSetting requiredFilledSlots;
    private final IntSetting cycleDelayMs;
    private static final long GUI_WAIT_TIMEOUT_MS = 5000L;
    private static final long DELAY_MINIMAL_MS = 50L;
    private static final long DELAY_AFTER_CMD_MS = 300L;
    private static final long DELAY_AFTER_DEPOSIT_MS = 250L;
    private static final long DELAY_AFTER_SELL_CLICK_MS = 350L;
    private static final long DELAY_TOPUP_RETRY_MS = 500L;
    private static final int MAX_OPEN_GUI_RETRIES = 5;
    private static final int EMPTY_CYCLE_WARN_EVERY = 5;
    private static final String[] CONFIRM_KEYWORDS;
    private static final Set COMMON_CONFIRM_ITEMS;
    private final Set parsedTargets;
    private String parsedTargetsSource;
    private AutoSell.State state;
    private int openGuiRetries;
    private int emptyCycleStreak;
    private long delayUntilMs;
    private long guiWaitDeadlineMs;

    public AutoSell() {
        super("AutoSell", "GUI-based autosell state machine (DonutSMP)", Category.DONUT_SMP);
        this.targetItems = ((StringSetting) addSetting(new StringSetting("TargetItems", "Comma-separated item ids to sell", "minecraft:cobblestone_wall")));
        this.sellCommand = ((StringSetting) addSetting(new StringSetting("SellCommand", "Command to open sell GUI", "/sell")));
        this.sellButtonSlot = ((IntSetting) addSetting(new IntSetting("SellButtonSlot", "Fallback slot index for sell button", 49, 0, 200)));
        this.requiredFilledSlots = ((IntSetting) addSetting(new IntSetting("RequiredFilledSlots", "Sell when this many container slots are filled (0 = auto)", 45, 0, 200)));
        this.cycleDelayMs = ((IntSetting) addSetting(new IntSetting("CycleDelayMs", "Delay between sell cycles in milliseconds", 4000, 200, 60000, 50)));
        this.parsedTargets = new LinkedHashSet();
        this.parsedTargetsSource = "";
        this.state = AutoSell.State.IDLE;
        this.openGuiRetries = 0;
        this.emptyCycleStreak = 0;
        this.delayUntilMs = 0L;
        this.guiWaitDeadlineMs = 0L;
    }

    public void onEnable() {
        super.onEnable();
        resetRuntime();
        refreshParsedTargets();
    }

    public void onDisable() {
        super.onDisable();
        resetRuntime();
    }

    public void onTick() {
        if (!(isInGame())) return;
        refreshParsedTargets();
        if (System.currentTimeMillis() < this.delayUntilMs) return;
        switch (this.state.ordinal()) {
            case 0 -> { break label_4; }
            case 1 -> { break label_5; }
            case 2 -> { break label_6; }
            case 3 -> { break label_7; }
            case 4 -> { break label_8; }
            case 5 -> { break label_9; }
            case 6 -> { break label_10; }
            default -> { break label_12; }
        }
        label_4:
        this.state = AutoSell.State.OPEN_SELL_GUI;
        break label_12;
        label_5:
        handleOpenSellGui();
        break label_12;
        label_6:
        handleWaitSellGui();
        break label_12;
        label_7:
        handleDepositItems();
        break label_12;
        label_8:
        handleClickSellButton();
        break label_12;
        label_9:
        handleAfterSellCheck();
        break label_12;
        label_10:
        setDelay(((long) ((Integer) this.cycleDelayMs.get()).intValue()));
        this.state = AutoSell.State.IDLE;
        label_12:
    }

    private void handleOpenSellGui() {
        if ((mc.field_1755 instanceof class_465)) {
            this.openGuiRetries = 0;
            setDelay(50L);
            this.state = AutoSell.State.DEPOSIT_ITEMS;
            return;
        }
        if (this.parsedTargets.isEmpty()) {
            warn("No valid target items. Set TargetItems first.");
            setEnabled(false);
            return;
        }
        if (this.openGuiRetries >= 5) {
            warn("Cannot open sell GUI after 5 retries. Stopping.");
            setEnabled(false);
            return;
        }
        sendCommand(((String) this.sellCommand.get()));
        this.openGuiRetries = (this.openGuiRetries + 1);
        setDelay(300L);
        this.guiWaitDeadlineMs = (System.currentTimeMillis() + 5000L);
        this.state = AutoSell.State.WAIT_SELL_GUI;
    }

    private void handleWaitSellGui() {
        if ((mc.field_1755 instanceof class_465)) {
            this.openGuiRetries = 0;
            setDelay(50L);
            this.state = AutoSell.State.DEPOSIT_ITEMS;
            return;
        }
        if (System.currentTimeMillis() >= this.guiWaitDeadlineMs) {
            this.state = AutoSell.State.OPEN_SELL_GUI;
        }
    }

    private void handleDepositItems() {
        class_1703 handler = mc.field_1755;
        if (!((handler instanceof class_465))) break label_2;
        class_465 screen = ((class_465) handler);
        break label_4;
        label_2:
        this.state = AutoSell.State.OPEN_SELL_GUI;
        return;
        label_4:
        if ((mc.field_1761 == null) || (mc.field_1724 == null)) {
            return;
        }
        handler = screen.method_17577();
        int totalSlots = handler.field_7761.size();
        int playerInvStart = Math.max(0, (totalSlots - 36));
        int movedStacks = 0;
        int slot = playerInvStart;
        while (slot < totalSlots) {
            class_1799 stack = handler.method_7611(slot).method_7677();
            if (!(matchesTarget(stack))) {
            } else {
                mc.field_1761.method_2906(handler.field_7763, slot, 0, class_1713.field_7794, mc.field_1724);
                movedStacks++;
            }
            slot++;
        }
        int confirmSlot = findConfirmSlot(handler, playerInvStart);
        int filledItemSlots = countFilledItemSlots(handler, playerInvStart, confirmSlot);
        int capacity = effectiveCapacity(confirmSlot, playerInvStart);
        if (capacity <= 0) break label_21;
        boolean basketFull = filledItemSlots >= capacity;
        if (!(!basketFull)) {
            this.emptyCycleStreak = 0;
            setDelay(250L);
            this.state = AutoSell.State.CLICK_SELL_BUTTON;
            return;
        }
        if (movedStacks == 0) {
            if (filledItemSlots > 0) {
                this.emptyCycleStreak = 0;
                setDelay(250L);
                this.state = AutoSell.State.CLICK_SELL_BUTTON;
                return;
            }
            this.emptyCycleStreak = (this.emptyCycleStreak + 1);
            if (this.emptyCycleStreak != 1) {
                if ((this.emptyCycleStreak % 5) != 0) break label_38;
            }
            warn("No matching items found, waiting...");
            break label_38;
        }
        this.emptyCycleStreak = 0;
        label_38:
        setDelay(500L);
        this.state = AutoSell.State.DEPOSIT_ITEMS;
    }

    private void handleClickSellButton() {
        class_1703 handler = mc.field_1755;
        if ((handler instanceof class_465)) {
            class_465 screen = ((class_465) handler);
        } else {
            this.state = AutoSell.State.OPEN_SELL_GUI;
            return;
        }
        if ((mc.field_1761 == null) || (mc.field_1724 == null)) {
            return;
        }
        handler = screen.method_17577();
        int totalSlots = handler.field_7761.size();
        int containerSlotCount = Math.max(0, (totalSlots - 36));
        int confirmSlot = findConfirmSlot(handler, containerSlotCount);
        if (isValidSlot(confirmSlot, totalSlots)) {
            mc.field_1761.method_2906(handler.field_7763, confirmSlot, 0, class_1713.field_7790, mc.field_1724);
        } else {
            warn("Sell button not found. Set SellButtonSlot manually.");
        }
        setDelay(350L);
        this.state = AutoSell.State.AFTER_SELL_CHECK;
    }

    private void handleAfterSellCheck() {
        this.state = ((mc.field_1755 instanceof class_465) ? AutoSell.State.DEPOSIT_ITEMS : AutoSell.State.CYCLE_COOLDOWN);
    }

    private int findConfirmSlot(class_1703 handler, int containerSlotCount) {
        int commonItemFallback = -1;
        int slot = 0;
        while (slot < containerSlotCount) {
            class_1799 stack = handler.method_7611(slot).method_7677();
            if (stack.method_7960()) {
                break label_15;
            }
            String name = stripFormatting(stack.method_7964().getString()).toLowerCase(Locale.ROOT);
            class_2960 id = CONFIRM_KEYWORDS;
            int var8 = id.length;
            int var9 = 0;
            while (var9 < var8) {
                String keyword = id[var9];
                if (name.contains(keyword)) return slot;
                var9++;
            }
            if (commonItemFallback != -1) break label_15;
            id = class_7923.field_41178.method_10221(stack.method_7909());
            if (!(COMMON_CONFIRM_ITEMS.contains(id))) break label_15;
            commonItemFallback = slot;
            label_15:
            slot++;
        }
        if (commonItemFallback != -1) {
        } else {
        }
        return ((Integer) this.sellButtonSlot.get()).intValue();
    }

    private int effectiveCapacity(int confirmSlot, int containerSlotCount) {
        if (((Integer) this.requiredFilledSlots.get()).intValue() > 0) return Math.min(((Integer) this.requiredFilledSlots.get()).intValue(), containerSlotCount);
        return (containerSlotCount - (isValidSlot(confirmSlot, containerSlotCount) ? 1 : 0));
    }

    private int countFilledItemSlots(class_1703 handler, int containerSlotCount, int confirmSlot) {
        int count = 0;
        int slot = 0;
        while (slot < containerSlotCount) {
            if (slot == confirmSlot) {
                break label_5;
            }
            if (handler.method_7611(slot).method_7677().method_7960()) break label_5;
            count++;
            label_5:
            slot++;
        }
        return count;
    }

    private boolean matchesTarget(class_1799 stack) {
        if (stack.method_7960()) return false;
        class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
        return this.parsedTargets.contains(id);
    }

    private void refreshParsedTargets() {
        String raw = ((String) this.targetItems.get());
        if (raw == null) {
            raw = "";
        }
        if (raw.equals(this.parsedTargetsSource)) return;
        this.parsedTargetsSource = raw;
        this.parsedTargets.clear();
        String[] tokens = raw.split(",");
        Object var3 = tokens;
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            String token = var3[var5];
            class_2960 id = parseItemId(token);
            if (id == null) {
                break label_12;
            }
            if (!(class_7923.field_41178.method_10235().contains(id))) {
                break label_12;
            }
            this.parsedTargets.add(id);
            label_12:
            var5++;
        }
    }

    private class_2960 parseItemId(String token) {
        if (token == null) return null;
        String raw = token.trim().toLowerCase(Locale.ROOT);
        if (raw.isEmpty()) return null;
        if (raw.contains(":")) {
        } else {
        }
        String full = ("minecraft:" + raw);
        int sep = full.indexOf(58);
        if ((sep <= 0) || (sep >= (full.length() - 1))) {
            return null;
        }
        String namespace = full.substring(0, sep);
        String path = full.substring((sep + 1));
        try {
            return class_2960.method_60655(namespace, path);
        } catch (Exception ex13) {
            Exception ignored = ex13;
            return null;
        }
    }

    private boolean isValidSlot(int slot, int bound) {
        if (slot < 0) break label_1;
        return slot < bound;
    }

    private String stripFormatting(String text) {
        if (text == null) return "";
        return text.replaceAll("\\u00A7[0-9a-fk-or]", "");
    }

    private void setDelay(long ms) {
        this.delayUntilMs = (System.currentTimeMillis() + ms);
    }

    private void sendCommand(String raw) {
        if ((mc.field_1724 == null) || (mc.field_1724.field_3944 == null)) {
            return;
        }
        if (raw == null) {
        } else {
        }
        String cmd = raw.trim();
        if (cmd.isEmpty()) {
            cmd = "/sell";
        }
        if (cmd.startsWith("/")) {
            mc.field_1724.field_3944.method_45730(cmd.substring(1));
        } else {
            mc.field_1724.field_3944.method_45729(cmd);
        }
    }

    private void warn(String msg) {
        if (mc.field_1724 == null) return;
        mc.field_1724.method_7353(class_2561.method_43470(("\u00a7d[VC]\u00a77 [AutoSell] " + msg)), false);
    }

    private void resetRuntime() {
        this.state = AutoSell.State.IDLE;
        this.openGuiRetries = 0;
        this.emptyCycleStreak = 0;
        this.delayUntilMs = 0L;
        this.guiWaitDeadlineMs = 0L;
    }

    public String getInfo() {
        switch (this.state.ordinal()) {
            case 0 -> { break label_2; }
            case 1 -> { break label_3; }
            case 2 -> { break label_4; }
            case 3 -> { break label_5; }
            case 4 -> { break label_6; }
            case 5 -> { break label_7; }
            case 6 -> { break label_8; }
            default -> { break label_1; }
        }
        label_1:
        throw new MatchException(null, null);
        label_2:
        break label_9;
        label_3:
        break label_9;
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
        return "Cooldown";
    }

    static {
        var tmp0 = new String[10];
        tmp0[0] = "sell";
        tmp0[1] = "confirm";
        tmp0[2] = "accept";
        tmp0[3] = "ok";
        tmp0[4] = "ban";
        tmp0[5] = "b\u00e1n";
        tmp0[6] = "xac nhan";
        tmp0[7] = "x\u00e1c nh\u1eadn";
        tmp0[8] = "dong y";
        tmp0[9] = "\u0111\u1ed3ng \u00fd";
        CONFIRM_KEYWORDS = tmp0;
        COMMON_CONFIRM_ITEMS = Set.of(class_2960.method_60655("minecraft", "lime_stained_glass_pane"), class_2960.method_60655("minecraft", "lime_dye"), class_2960.method_60655("minecraft", "lime_wool"), class_2960.method_60655("minecraft", "lime_concrete"), class_2960.method_60655("minecraft", "emerald"), class_2960.method_60655("minecraft", "emerald_block"), class_2960.method_60655("minecraft", "slime_ball"));
    }


    enum State {
        IDLE,
        OPEN_SELL_GUI,
        WAIT_SELL_GUI,
        DEPOSIT_ITEMS,
        CLICK_SELL_BUTTON,
        AFTER_SELL_CHECK,
        CYCLE_COOLDOWN;


        private State() {
        }

    }
}
