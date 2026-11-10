package me.drake.viecheats.modules.combat;

import java.util.Random;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.EnumSetting;
import me.drake.viecheats.core.setting.IntSetting;
import me.drake.viecheats.util.EntityUtil;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;

public class Triggerbot extends Module {
    private final IntSetting delay;
    private final IntSetting jitter;
    private final EnumSetting filter;
    private final BoolSetting friendCheck;
    private long nextAttack;
    private final Random random;

    public Triggerbot() {
        super("Triggerbot", "Attacks when crosshair is on entity", Category.COMBAT);
        this.delay = ((IntSetting) addSetting(new IntSetting("Delay", "Base attack delay (ms)", 200, 100, 500)));
        this.jitter = ((IntSetting) addSetting(new IntSetting("JitterDelay", "Random delay variation (ms)", 30, 0, 100)));
        this.filter = ((EnumSetting) addSetting(new EnumSetting("Filter", "Entity filter", EntityUtil.Filter.PLAYERS, EntityUtil.Filter.values())));
        this.friendCheck = ((BoolSetting) addSetting(new BoolSetting("FriendCheck", "Don't attack friends", true)));
        this.nextAttack = 0L;
        this.random = new Random();
    }

    public void onTick() {
        if (!(isInGame())) return;
        KillAura ka = ((KillAura) VieCheatsMod.getInstance().getModuleManager().getModule(KillAura.class));
        if (ka != null && ka.isEnabled()) return;
        class_1297 target = mc.field_1692;
        if (target == null) return;
        if ((target instanceof class_1309)) {
            class_1309 living = ((class_1309) target);
        } else {
            return;
        }
        if ((living.method_29504()) || (living.method_6032() <= 0.0f)) {
            return;
        }
        if (!(EntityUtil.passesFilter(living, ((EntityUtil.Filter) this.filter.get()), ((Boolean) this.friendCheck.get()).booleanValue()))) return;
        long now = System.currentTimeMillis();
        if (now < this.nextAttack) return;
        float cooldown = mc.field_1724.method_7261(0.5f);
        if (cooldown < 0.9f) return;
        mc.field_1761.method_2918(mc.field_1724, living);
        mc.field_1724.method_6104(class_1268.field_5808);
        long d = (((long) ((Integer) this.delay.get()).intValue()) + ((long) (this.random.nextGaussian() * ((double) ((Integer) this.jitter.get()).intValue()))));
        this.nextAttack = (now + Math.max(50L, d));
    }

}
