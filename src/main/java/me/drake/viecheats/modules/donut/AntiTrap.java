package me.drake.viecheats.modules.donut;

import java.util.ArrayList;
import java.util.List;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.EntitySpawnEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.util.EntityCache;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1531;
import net.minecraft.class_1694;
import net.minecraft.class_1695;
import net.minecraft.class_2561;
import net.minecraft.class_6374;

public class AntiTrap extends Module {
    private final EnumSetting escape;
    private int trapTicks;
    private int checkTicks;
    private static final int TRAP_THRESHOLD = 3;
    private static final int CHECK_INTERVAL = 5;

    public AntiTrap() {
        super("AntiTrap", "Detects and escapes from traps (DonutSMP)", Category.DONUT_SMP);
        this.escape = ((EnumSetting) addSetting(new EnumSetting("EscapeMode", "How to escape when trapped", AntiTrap.EscapeMode.ATTACK, AntiTrap.EscapeMode.values())));
        this.trapTicks = 0;
        this.checkTicks = 0;
    }

    public boolean usesEntityCache() {
        return true;
    }

    public void onEnable() {
        super.onEnable();
        removeTrapEntities();
    }

    public void onTick() {
        if (!(isInGame())) return;
        this.checkTicks = (this.checkTicks + 1);
        if (this.checkTicks < 5) return;
        this.checkTicks = 0;
        if (isTrapped()) {
            this.trapTicks = (this.trapTicks + 1);
            if (this.trapTicks < 3) break label_10;
            triggerEscape();
            this.trapTicks = 0;
            break label_10;
        }
        this.trapTicks = Math.max(0, (this.trapTicks - 1));
        label_10:
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (isTrapEntity(event.getEntityType())) {
            event.setCancelled(true);
        }
    }

    private boolean isTrapEntity(class_1299 type) {
        if (type != class_1299.field_6131) {
            if (type == class_1299.field_6126) {
            }
        } else {
        }
        return false;
    }

    private void removeTrapEntities() {
        if (mc.field_1687 == null) return;
        List toRemove = new ArrayList();
        mc.field_1687.method_18112().forEach((entity) -> {
    if (entity == null) break label_2;
    if (!((entity instanceof class_1531))) {
        if (!((entity instanceof class_1694))) break label_2;
    }
    toRemove.add(entity);
    label_2:
});
        Object var2 = toRemove.iterator();
        while (var2.hasNext()) {
            class_1297 entity = ((class_1297) var2.next());
            if (!(entity.method_31481())) {
                entity.method_5650(class_1297.class_5529.field_26999);
            }
        }
    }

    private boolean isTrapped() {
        Object var1 = EntityCache.getEntities().iterator();
        while (var1.hasNext()) {
            EntityCache.CachedEntity cached = ((EntityCache.CachedEntity) var1.next());
            class_1297 entity = cached.entity();
            if (entity == mc.field_1724) {
                break label_1;
            }
            if (!((entity instanceof class_1531))) {
                if ((entity instanceof class_1695)) {
                }
                if (entity.method_5829().method_994(mc.field_1724.method_5829())) return true;
            }
        }
        return false;
    }

    private void triggerEscape() {
        switch (((AntiTrap.EscapeMode) this.escape.get()).ordinal()) {
            case 0 -> { break label_1; }
            case 1 -> { break label_13; }
            default -> { break label_17; }
        }
        label_1:
        Object var1 = EntityCache.getEntities().iterator();
        while (var1.hasNext()) {
            EntityCache.CachedEntity cached = ((EntityCache.CachedEntity) var1.next());
            class_1297 entity = cached.entity();
            if (entity == mc.field_1724) {
                break label_2;
            }
            if (!((entity instanceof class_1531))) {
                if (!((entity instanceof class_1695))) break label_11;
            }
            if (!(entity.method_5829().method_994(mc.field_1724.method_5829()))) break label_11;
            mc.field_1761.method_2918(mc.field_1724, entity);
            mc.field_1724.method_6104(class_1268.field_5808);
            break label_12;
            label_11:
        }
        label_12:
        break label_17;
        label_13:
        mc.field_1724.field_3944.method_52787(new class_6374(0));
        mc.method_1562().method_48296().method_10747(class_2561.method_43470("AntiTrap \u2014 Emergency disconnect"));
        label_17:
    }


    public enum EscapeMode {
        ATTACK,
        DISCONNECT;


        private EscapeMode() {
        }

    }
}
