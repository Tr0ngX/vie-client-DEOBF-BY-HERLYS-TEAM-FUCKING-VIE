package me.drake.viecheats.modules.donut;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.BoolSetting;
import me.drake.viecheats.core.setting.StringSetting;
import net.minecraft.class_2338;

public class CoordLogger extends Module {
    private static final int LOG_INTERVAL_SECS = 60;
    private final BoolSetting logOnDeath;
    private final StringSetting label;
    private int tickCounter;
    private static final DateTimeFormatter FMT;

    public CoordLogger() {
        super("CoordLogger", "Logs player coordinates to disk (DonutSMP)", Category.DONUT_SMP);
        this.logOnDeath = ((BoolSetting) addSetting(new BoolSetting("LogOnDeath", "Save coords when player dies", true)));
        this.label = ((StringSetting) addSetting(new StringSetting("DefaultLabel", "Default note label", "coord")));
        this.tickCounter = 0;
    }

    public void onTick() {
        if (!(isInGame())) return;
        this.tickCounter = (this.tickCounter + 1);
        if (this.tickCounter >= 1200) {
            this.tickCounter = 0;
            saveCoords(((String) this.label.get()));
        }
    }

    public void saveCoords(String note) {
        if (!(isInGame())) return;
        class_2338 pos = mc.field_1724.method_24515();
        String dim = mc.field_1687.method_27983().method_29177().method_12832();
        var tmp0 = new Object[6];
        tmp0[0] = LocalDateTime.now().format(FMT);
        tmp0[1] = note;
        tmp0[2] = dim;
        tmp0[3] = pos.method_10263();
        tmp0[4] = pos.method_10264();
        tmp0[5] = pos.method_10260();
        String line = String.format("[%s] %s | %s | %d, %d, %d%n", tmp0);
        appendToFile("coords.txt", line);
    }

    private void appendToFile(String filename, String content) {
        try {
            var tmp0 = new String[1];
            tmp0[0] = "viecheats";
            Path dir = Paths.get(mc.field_1697.getAbsolutePath(), tmp0);
            Files.createDirectories(dir, new FileAttribute[0]);
            var tmp1 = new OpenOption[2];
            tmp1[0] = StandardOpenOption.CREATE;
            tmp1[1] = StandardOpenOption.APPEND;
            Files.writeString(dir.resolve(filename), content, tmp1);
        } catch (IOException ex4) {
            IOException e = ex4;
            VieCheatsMod.LOGGER.error("[CoordLogger] Failed to write: {}", e.getMessage());
        }
    }

    static {
        FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

}
