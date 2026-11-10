package me.drake.viecheats.modules.donut;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.event.EventHandler;
import me.drake.viecheats.core.event.events.TickEvent;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.IntSetting;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2636;
import net.minecraft.class_2680;
import net.minecraft.class_3417;

public class RTPBaseFinder extends Module {
    private final IntSetting cooldown;
    private final IntSetting scanRadius;
    private final BoolSetting autoLog;
    private final IntSetting minScore;
    private final BoolSetting chatNotify;
    private final BoolSetting soundAlert;
    private int cooldownTicks;
    private boolean waitingForTeleport;
    private double lastX;
    private double lastZ;
    private int scanDelayTicks;
    private final Set loggedCoords;
    private static final int SCORE_CHEST = 2;
    private static final int SCORE_BARREL = 2;
    private static final int SCORE_SHULKER = 5;
    private static final int SCORE_ENDER_CHEST = 4;
    private static final int SCORE_FURNACE = 1;
    private static final int SCORE_BLAST_FURNACE = 2;
    private static final int SCORE_SMOKER = 2;
    private static final int SCORE_BREWING = 3;
    private static final int SCORE_ANVIL = 3;
    private static final int SCORE_ENCHANTING = 4;
    private static final int SCORE_BEACON = 10;
    private static final int SCORE_HOPPER = 1;
    private static final int SCORE_DISPENSER = 1;
    private static final int SCORE_DROPPER = 1;
    private static final int SCORE_SPAWNER = 8;
    private static final int SCORE_BED = 3;
    private static final int SCORE_CRAFTING_TABLE = 1;

    public RTPBaseFinder() {
        super("RTPBaseFinder", "Auto /rtp + scan for bases", Category.DONUT_SMP);
        this.cooldown = ((IntSetting) addSetting(new IntSetting("Cooldown", "Seconds between RTPs", 30, 10, 120, 5)));
        this.scanRadius = ((IntSetting) addSetting(new IntSetting("ScanRadius", "Block scan radius", 48, 8, 96, 4)));
        this.autoLog = ((BoolSetting) addSetting(new BoolSetting("AutoLog", "Log bases to file", true)));
        this.minScore = ((IntSetting) addSetting(new IntSetting("MinScore", "Minimum base score to flag (chests=2, shulkers=5, spawners=10, etc)", 10, 1, 50, 1)));
        this.chatNotify = ((BoolSetting) addSetting(new BoolSetting("ChatNotify", "Notify in chat when base found", true)));
        this.soundAlert = ((BoolSetting) addSetting(new BoolSetting("SoundAlert", "Play sound on base found", true)));
        this.cooldownTicks = 0;
        this.waitingForTeleport = false;
        this.scanDelayTicks = 0;
        this.loggedCoords = new HashSet();
    }

    public void onEnable() {
        super.onEnable();
        this.cooldownTicks = 20;
        this.waitingForTeleport = false;
        this.loggedCoords.clear();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (event.getPhase() != TickEvent.Phase.PRE) return;
        if (!(isInGame())) return;
        if (!(!this.waitingForTeleport)) {
            double dx = (mc.field_1724.method_23317() - this.lastX);
            double dz = (mc.field_1724.method_23321() - this.lastZ);
            if (Math.abs(dx) <= 50.0) {
                if (Math.abs(dz) > 50.0) {
                }
                this.waitingForTeleport = false;
                this.scanDelayTicks = 60;
            }
            return;
        }
        if (this.scanDelayTicks > 0) {
            this.scanDelayTicks = (this.scanDelayTicks - 1);
            if (!this.scanDelayTicks) {
                scanArea();
            }
            return;
        }
        if (this.cooldownTicks > 0) {
            this.cooldownTicks = (this.cooldownTicks - 1);
            return;
        }
        this.lastX = mc.field_1724.method_23317();
        this.lastZ = mc.field_1724.method_23321();
        mc.field_1724.field_3944.method_45730("rtp");
        this.waitingForTeleport = true;
        this.cooldownTicks = (((Integer) this.cooldown.get()).intValue() * 20);
    }

    private void scanArea() {
        int radius = ((Integer) this.scanRadius.get()).intValue();
        class_2338 center = mc.field_1724.method_24515();
        int totalScore = 0;
        int chests = 0;
        int barrels = 0;
        int shulkers = 0;
        int enderChests = 0;
        int furnaces = 0;
        int brewingStands = 0;
        int anvils = 0;
        int enchantTables = 0;
        int beacons = 0;
        int hoppers = 0;
        int spawners = 0;
        int beds = 0;
        int craftingTables = 0;
        int x = -(radius);
        while (x <= radius) {
            int y = (-(radius) / 2);
            while (y <= (radius / 2)) {
                int z = -(radius);
                while (z <= radius) {
                    class_2338 pos = center.method_10069(x, y, z);
                    class_2680 state = mc.field_1687.method_8320(pos);
                    class_2248 block = state.method_26204();
                    if (block != class_2246.field_10034) {
                        if (block == class_2246.field_10380) {
                        }
                        chests++;
                        totalScore += 2;
                        break label_57;
                    }
                    if (block == class_2246.field_16328) {
                        barrels++;
                        totalScore += 2;
                        break label_57;
                    }
                    if (block == class_2246.field_10443) {
                        enderChests++;
                        totalScore += 4;
                        break label_57;
                    }
                    if (block == class_2246.field_10312) {
                        hoppers++;
                        totalScore++;
                        break label_57;
                    }
                    if (block == class_2246.field_10200) {
                        totalScore++;
                        break label_57;
                    }
                    if (block == class_2246.field_10228) {
                        totalScore++;
                        break label_57;
                    }
                    if (isShulkerBox(block)) {
                        shulkers++;
                        totalScore += 5;
                        break label_57;
                    }
                    if (block == class_2246.field_10181) {
                        furnaces++;
                        totalScore++;
                        break label_57;
                    }
                    if (block == class_2246.field_16333) {
                        furnaces++;
                        totalScore += 2;
                        break label_57;
                    }
                    if (block == class_2246.field_16334) {
                        furnaces++;
                        totalScore += 2;
                        break label_57;
                    }
                    if (block == class_2246.field_10333) {
                        brewingStands++;
                        totalScore += 3;
                        break label_57;
                    }
                    if (block != class_2246.field_10535 && block != class_2246.field_10105) {
                        if (block == class_2246.field_10414) {
                        }
                        anvils++;
                        totalScore += 3;
                        break label_57;
                    }
                    if (block == class_2246.field_10485) {
                        enchantTables++;
                        totalScore += 4;
                        break label_57;
                    }
                    if (block == class_2246.field_10327) {
                        beacons++;
                        totalScore += 10;
                        break label_57;
                    }
                    if (block == class_2246.field_9980) {
                        craftingTables++;
                        totalScore++;
                        break label_57;
                    }
                    if (!(isBed(block))) break label_57;
                    beds++;
                    totalScore += 3;
                    label_57:
                    class_2586 be = mc.field_1687.method_8321(pos);
                    if ((be instanceof class_2636)) {
                        spawners++;
                        totalScore += 8;
                    }
                    z++;
                }
                y++;
            }
            x++;
        }
        if (totalScore < ((Integer) this.minScore.get()).intValue()) break label_86;
        String dim = mc.field_1687.method_27983().method_29177().method_12832();
        var tmp0 = new Object[2];
        tmp0[0] = mc.field_1724.method_23317();
        tmp0[1] = mc.field_1724.method_23321();
        String coordKey = String.format("%.0f/%.0f", tmp0);
        if (((Boolean) this.chatNotify.get()).booleanValue()) {
            var tmp1 = new Object[5];
            tmp1[0] = totalScore;
            tmp1[1] = mc.field_1724.method_23317();
            tmp1[2] = mc.field_1724.method_23318();
            tmp1[3] = mc.field_1724.method_23321();
            tmp1[4] = dim;
            String msg = String.format("\u00a7d[VC] \u00a7a\u2691 BASE FOUND! \u00a7fScore: \u00a7e%d \u00a77| \u00a7fXYZ: \u00a7a%.0f / %.0f / %.0f \u00a77[%s]", tmp1);
            mc.field_1724.method_7353(class_2561.method_43470(msg), false);
            StringBuilder details = new StringBuilder("\u00a7d[VC] \u00a77\u2192 ");
            if (chests > 0) {
                details.append("\u00a7fChests:\u00a7e").append(chests).append(" ");
            }
            if (shulkers > 0) {
                details.append("\u00a7dShulkers:\u00a7e").append(shulkers).append(" ");
            }
            if (enderChests > 0) {
                details.append("\u00a75EChests:\u00a7e").append(enderChests).append(" ");
            }
            if (spawners > 0) {
                details.append("\u00a7cSpawners:\u00a7e").append(spawners).append(" ");
            }
            if (beacons > 0) {
                details.append("\u00a7bBeacons:\u00a7e").append(beacons).append(" ");
            }
            if (furnaces > 0) {
                details.append("\u00a77Furnaces:\u00a7e").append(furnaces).append(" ");
            }
            if (brewingStands > 0) {
                details.append("\u00a79Brewing:\u00a7e").append(brewingStands).append(" ");
            }
            if (beds > 0) {
                details.append("\u00a7cBeds:\u00a7e").append(beds).append(" ");
            }
            mc.field_1724.method_7353(class_2561.method_43470(details.toString()), false);
        }
        if (((Boolean) this.soundAlert.get()).booleanValue()) {
            mc.field_1724.method_5783(class_3417.field_14627, 1.0f, 1.5f);
        }
        if ((!(((Boolean) this.autoLog.get()).booleanValue())) || (this.loggedCoords.contains(coordKey))) break label_86;
        logBase(totalScore, chests, shulkers, enderChests, spawners, beacons, furnaces, brewingStands, anvils, enchantTables, beds);
        this.loggedCoords.add(coordKey);
        label_86:
    }

    private boolean isShulkerBox(class_2248 block) {
        if (block != class_2246.field_10603 && block != class_2246.field_10199 && block != class_2246.field_10407 && block != class_2246.field_10063 && block != class_2246.field_10203 && block != class_2246.field_10600 && block != class_2246.field_10275 && block != class_2246.field_10051 && block != class_2246.field_10140 && block != class_2246.field_10320 && block != class_2246.field_10532 && block != class_2246.field_10268 && block != class_2246.field_10605 && block != class_2246.field_10373 && block != class_2246.field_10055 && block != class_2246.field_10068) {
            if (block == class_2246.field_10371) {
            }
        } else {
        }
        return false;
    }

    private boolean isBed(class_2248 block) {
        if (block != class_2246.field_10120 && block != class_2246.field_10410 && block != class_2246.field_10230 && block != class_2246.field_10621 && block != class_2246.field_10356 && block != class_2246.field_10180 && block != class_2246.field_10610 && block != class_2246.field_10141 && block != class_2246.field_10326 && block != class_2246.field_10109 && block != class_2246.field_10019 && block != class_2246.field_10527 && block != class_2246.field_10288 && block != class_2246.field_10561 && block != class_2246.field_10069) {
            if (block == class_2246.field_10461) {
            }
        } else {
        }
        return false;
    }

    private void logBase(int score, int chests, int shulkers, int enderChests, int spawners, int beacons, int furnaces, int brewing, int anvils, int enchant, int beds) {
        try {
            Path logFile = mc.field_1697.toPath().resolve("viecheats").resolve("baselog.txt");
            Files.createDirectories(logFile.getParent(), new FileAttribute[0]);
            String dim = mc.field_1687.method_27983().method_29177().method_12832();
            var tmp0 = new Object[16];
            tmp0[0] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            tmp0[1] = dim;
            tmp0[2] = mc.field_1724.method_23317();
            tmp0[3] = mc.field_1724.method_23318();
            tmp0[4] = mc.field_1724.method_23321();
            tmp0[5] = score;
            tmp0[6] = chests;
            tmp0[7] = shulkers;
            tmp0[8] = enderChests;
            tmp0[9] = spawners;
            tmp0[10] = beacons;
            tmp0[11] = furnaces;
            tmp0[12] = brewing;
            tmp0[13] = anvils;
            tmp0[14] = enchant;
            tmp0[15] = beds;
            String entry = String.format("[%s] [%s] XYZ: %.0f / %.0f / %.0f | Score: %d | Chests: %d | Shulkers: %d | EChests: %d | Spawners: %d | Beacons: %d | Furnaces: %d | Brewing: %d | Anvils: %d | Enchant: %d | Beds: %d%n", tmp0);
            var tmp1 = new OpenOption[2];
            tmp1[0] = StandardOpenOption.CREATE;
            tmp1[1] = StandardOpenOption.APPEND;
            Files.writeString(logFile, entry, StandardCharsets.UTF_8, tmp1);
        } catch (IOException ex11) {
            IOException e = ex11;
            VieCheatsMod.LOGGER.error("[RTPBaseFinder] Failed to write log", e);
        }
    }

    public String getInfo() {
        if (!(!this.waitingForTeleport)) return "Teleporting...";
        if (this.scanDelayTicks > 0) return "Scanning...";
        return ((this.cooldownTicks / 20) + "s");
    }

}
