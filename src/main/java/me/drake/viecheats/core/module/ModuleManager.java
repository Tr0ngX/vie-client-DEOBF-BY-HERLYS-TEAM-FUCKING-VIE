package me.drake.viecheats.core.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.modules.client.ClientSettings;
import me.drake.viecheats.modules.client.FakeRoles;
import me.drake.viecheats.modules.client.FakeStats;
import me.drake.viecheats.modules.client.HUD;
import me.drake.viecheats.modules.client.NameProtect;
import me.drake.viecheats.modules.combat.AutoArmor;
import me.drake.viecheats.modules.combat.AutoTotem;
import me.drake.viecheats.modules.combat.CrystalAura;
import me.drake.viecheats.modules.combat.KillAura;
import me.drake.viecheats.modules.combat.SpearAura;
import me.drake.viecheats.modules.combat.Triggerbot;
import me.drake.viecheats.modules.combat.Velocity;
import me.drake.viecheats.modules.donut.AHSniper;
import me.drake.viecheats.modules.donut.AntiTrap;
import me.drake.viecheats.modules.donut.AutoCrop;
import me.drake.viecheats.modules.donut.AutoSell;
import me.drake.viecheats.modules.donut.AutoTreeFarmer;
import me.drake.viecheats.modules.donut.BoneDropper;
import me.drake.viecheats.modules.donut.ChunkFinder;
import me.drake.viecheats.modules.donut.CoordLogger;
import me.drake.viecheats.modules.donut.ElytraSwap;
import me.drake.viecheats.modules.donut.EmergencySeller;
import me.drake.viecheats.modules.donut.RTPBaseFinder;
import me.drake.viecheats.modules.donut.RegionMap;
import me.drake.viecheats.modules.donut.SpawnersNotifier;
import me.drake.viecheats.modules.donut.SusChunkFinder;
import me.drake.viecheats.modules.exploit.Freecam;
import me.drake.viecheats.modules.exploit.PacketFly;
import me.drake.viecheats.modules.exploit.Timer;
import me.drake.viecheats.modules.movement.AutoSprint;
import me.drake.viecheats.modules.movement.Fly;
import me.drake.viecheats.modules.movement.FreeLook;
import me.drake.viecheats.modules.movement.NoFall;
import me.drake.viecheats.modules.movement.Scaffold;
import me.drake.viecheats.modules.movement.Speed;
import me.drake.viecheats.modules.movement.Step;
import me.drake.viecheats.modules.movement.Tower;
import me.drake.viecheats.modules.player.AutoEat;
import me.drake.viecheats.modules.player.AutoFish;
import me.drake.viecheats.modules.player.AutoTool;
import me.drake.viecheats.modules.player.ChestStealer;
import me.drake.viecheats.modules.player.FastUse;
import me.drake.viecheats.modules.player.HomeSetter;
import me.drake.viecheats.modules.player.InvManager;
import me.drake.viecheats.modules.player.NoSlow;
import me.drake.viecheats.modules.player.SwingSpeed;
import me.drake.viecheats.modules.render.BedrockHoleESP;
import me.drake.viecheats.modules.render.BlockESP;
import me.drake.viecheats.modules.render.FullBright;
import me.drake.viecheats.modules.render.MobESP;
import me.drake.viecheats.modules.render.NameTags;
import me.drake.viecheats.modules.render.PlayerESP;
import me.drake.viecheats.modules.render.SpotifyHUD;
import me.drake.viecheats.modules.render.StorageESP;
import me.drake.viecheats.modules.render.SuspiciousESP;
import me.drake.viecheats.modules.render.Tracers;
import me.drake.viecheats.modules.world.AutoMine;
import me.drake.viecheats.modules.world.AutoPlace;
import me.drake.viecheats.modules.world.AutoSign;
import me.drake.viecheats.modules.world.Dig;
import me.drake.viecheats.modules.world.FastPlace;
import me.drake.viecheats.modules.world.NoBreakDelay;
import me.drake.viecheats.modules.world.Nuker;
import net.minecraft.class_310;

public class ModuleManager {
    private final List modules;
    private final Map modulesByClass;

    public ModuleManager() {
        this.modules = new ArrayList();
        this.modulesByClass = new HashMap();
        registerModules();
        VieCheatsMod.LOGGER.info("[VieCheat] Registered {} modules.", this.modules.size());
    }

    private void registerModules() {
        register(new KillAura());
        register(new SpearAura());
        register(new CrystalAura());
        register(new AutoTotem());
        register(new AutoArmor());
        register(new Velocity());
        register(new Triggerbot());
        register(new Fly());
        register(new Speed());
        register(new Step());
        register(new FreeLook());
        register(new AutoSprint());
        register(new NoFall());
        register(new Scaffold());
        register(new Tower());
        register(new AutoEat());
        register(new AutoFish());
        register(new ChestStealer());
        register(new InvManager());
        register(new AutoTool());
        register(new NoSlow());
        register(new FastUse());
        register(new SwingSpeed());
        register(new HomeSetter());
        register(new PlayerESP());
        register(new StorageESP());
        register(new BlockESP());
        register(new BedrockHoleESP());
        register(new MobESP());
        register(new FullBright());
        register(new Tracers());
        register(new NameTags());
        register(new SpotifyHUD());
        register(new SuspiciousESP());
        register(new Nuker());
        register(new AutoMine());
        register(new FastPlace());
        register(new NoBreakDelay());
        register(new AutoPlace());
        register(new AutoSign());
        register(new Dig());
        register(new Freecam());
        register(new Timer());
        register(new PacketFly());
        register(new AHSniper());
        register(new AutoSell());
        register(new RTPBaseFinder());
        register(new CoordLogger());
        register(new ElytraSwap());
        register(new AntiTrap());
        register(new EmergencySeller());
        register(new BoneDropper());
        register(new AutoTreeFarmer());
        register(new AutoCrop());
        register(new RegionMap());
        register(new SpawnersNotifier());
        register(new ChunkFinder());
        register(new SusChunkFinder());
        HUD hud = new HUD();
        register(hud);
        hud.setEnabled(true);
        ClientSettings clientSettings = new ClientSettings();
        register(clientSettings);
        register(new FakeStats());
        register(new NameProtect());
        register(new FakeRoles());
    }

    private void register(Module module) {
        this.modules.add(module);
        this.modulesByClass.put(module.getClass(), module);
    }

    public void onTick() {
        class_310 mc = class_310.method_1551();
        if ((mc == null) || (mc.field_1724 == null)) break label_2;
        boolean inGame = mc.field_1687 != null;
        Object var3 = this.modules.iterator();
        while (var3.hasNext()) {
            Module module = ((Module) var3.next());
            if (!(module.isEnabled())) {
                break label_5;
            }
            if (!inGame && module.getCategory() != Category.CLIENT) {
                break label_5;
            }
            module.onTick();
        }
    }

    public void onKeyPress(int keyCode) {
        toggleByBindCode(keyCode);
    }

    public void onMousePress(int mouseButton) {
        toggleByBindCode((1000 + mouseButton));
    }

    private void toggleByBindCode(int bindCode) {
        Object var2 = this.modules.iterator();
        while (var2.hasNext()) {
            Module module = ((Module) var2.next());
            if (module.getKeybind().isBound() && ((Integer) module.getKeybind().get()).intValue() == bindCode) {
                module.toggle();
            }
        }
    }

    public int disableProtectedModules() {
        int disabled = 0;
        Object var2 = this.modules.iterator();
        while (var2.hasNext()) {
            Module module = ((Module) var2.next());
            if (module.getCategory() == Category.CLIENT) {
                break label_2;
            }
            if (!(module.isEnabled())) {
                break label_2;
            }
            module.setEnabled(false);
            disabled++;
        }
        return disabled;
    }

    public Module getModule(Class clazz) {
        return ((Module) this.modulesByClass.get(clazz));
    }

    public Module getModuleByName(String name) {
        Object var2 = this.modules.iterator();
        while (var2.hasNext()) {
            Module mod = ((Module) var2.next());
            if (mod.getName().equalsIgnoreCase(name)) return mod;
        }
        return null;
    }

    public List getModules() {
        return Collections.unmodifiableList(this.modules);
    }

    public List getModules(Category category) {
        return ((List) this.modules.stream().filter((m) -> m.getCategory() == category).collect(Collectors.toList()));
    }

    public List getEnabled() {
        return ((List) this.modules.stream().filter(Module::isEnabled).collect(Collectors.toList()));
    }

    public boolean hasEnabledNonClientModules() {
        Object var1 = this.modules.iterator();
        while (var1.hasNext()) {
            Module module = ((Module) var1.next());
            if (module.getCategory() == Category.CLIENT) {
                break label_1;
            }
            if (module.isEnabled()) return true;
        }
        return false;
    }

    public boolean hasEnabledEntityCacheConsumers() {
        Object var1 = this.modules.iterator();
        while (var1.hasNext()) {
            Module module = ((Module) var1.next());
            if (!(module.isEnabled())) {
                break label_1;
            }
            if (module.usesEntityCache()) return true;
        }
        return false;
    }

    public void onGameOptionsLoad() {
        Object var1 = this.modules.iterator();
        while (var1.hasNext()) {
            Module module = ((Module) var1.next());
            if (module.isEnabled()) {
                module.onEnable();
            }
        }
    }

}
