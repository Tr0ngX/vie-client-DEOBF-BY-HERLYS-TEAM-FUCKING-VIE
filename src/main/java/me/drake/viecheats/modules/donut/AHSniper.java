package me.drake.viecheats.modules.donut;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.core.setting.StringSetting;
import me.drake.viecheats.util.ChatMessageQueue;

public class AHSniper extends Module {
    private final StringSetting targetItem;
    private final IntSetting maxPrice;
    private final IntSetting reactionDelay;
    private final StringSetting buyCommand;
    private long pendingBuyAt;
    private String pendingCmd;

    public AHSniper() {
        super("AHSniper", "Snipers cheap AH listings on DonutSMP", Category.DONUT_SMP);
        this.targetItem = ((StringSetting) addSetting(new StringSetting("ItemFilter", "Item ID to snipe (partial match)", "diamond")));
        this.maxPrice = ((IntSetting) addSetting(new IntSetting("MaxPrice", "Buy if price is below this", 1000, 1, 100000)));
        this.reactionDelay = ((IntSetting) addSetting(new IntSetting("ReactionDelay", "Delay before buying (ms)", 100, 0, 500)));
        this.buyCommand = ((StringSetting) addSetting(new StringSetting("BuyCommand", "Buy command template (%id% = listing ID)", "/ah buy %id%")));
        this.pendingBuyAt = -1L;
        this.pendingCmd = null;
    }

    public void onTick() {
        if (!(isInGame())) return;
        label_1:
        var tmp0 = ChatMessageQueue.poll();
        String msg = tmp0;
        if (tmp0 != null) {
            processMessage(msg);
            break label_1;
        }
        if (this.pendingBuyAt > 0L && System.currentTimeMillis() >= this.pendingBuyAt && this.pendingCmd != null) {
            mc.field_1724.field_3944.method_45729(this.pendingCmd);
            this.pendingBuyAt = -1L;
            this.pendingCmd = null;
        }
    }

    private void processMessage(String msg) {
        String lower = msg.toLowerCase();
        if (!(lower.contains("[ah]")) && !(lower.contains("auction"))) return;
        if (!(lower.contains(((String) this.targetItem.get()).toLowerCase()))) return;
        int price = extractPrice(msg);
        if ((price <= 0) || (price > ((Integer) this.maxPrice.get()).intValue())) {
            return;
        }
        String cmd = extractBuyCommand(msg);
        if (cmd == null) {
            cmd = ((String) this.buyCommand.get()).replace("%id%", "");
        }
        this.pendingCmd = cmd;
        this.pendingBuyAt = (System.currentTimeMillis() + ((long) ((Integer) this.reactionDelay.get()).intValue()));
    }

    private int extractPrice(String msg) {
        Matcher m = Pattern.compile("\\$(\\d+(?:,\\d{3})*)").matcher(msg);
        if (!(m.find())) break label_6;
        try {
            return Integer.parseInt(m.group(1).replace(",", ""));
        } catch (NumberFormatException ex5) {
            Object var3 = ex5;
            label_6:
            return -1;
        }
    }

    private String extractBuyCommand(String msg) {
        Matcher m = Pattern.compile("(/ah buy \\S+)").matcher(msg);
        if (m.find()) {
        } else {
        }
        return null;
    }

    public String getInfo() {
        return ((String) this.targetItem.get());
    }

}
