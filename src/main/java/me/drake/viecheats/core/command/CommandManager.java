package me.drake.viecheats.core.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.drake.viecheats.VieCheatsMod;
import me.drake.viecheats.auth.AuthManager;
import me.drake.viecheats.core.module.Category;
import me.drake.viecheats.core.module.Module;
import me.drake.viecheats.core.setting.Setting;
import me.drake.viecheats.gui.ClickGUI;
import me.drake.viecheats.modules.client.NameProtect;
import net.minecraft.class_2561;
import net.minecraft.class_310;

public class CommandManager {
    private static final class_310 mc;
    private final List commands;
    public static final String PREFIX = ".";

    public CommandManager() {
        this.commands = new ArrayList();
        registerCommands();
        VieCheatsMod.LOGGER.info("[VieCheat] Registered {} commands.", this.commands.size());
    }

    private void registerCommands() {
        register(new CommandManager.1(this, "vc", "Show client info", ".vc", new String[0]));
        register(new CommandManager.2(this, "auth", "Check license auth status", ".auth <check/status>", new String[0]));
        var tmp0 = new String[1];
        tmp0[0] = "t";
        register(new CommandManager.3(this, "toggle", "Toggle a module", ".toggle <module>", tmp0));
        var tmp1 = new String[1];
        tmp1[0] = "b";
        register(new CommandManager.4(this, "bind", "Set module keybind", ".bind <module> <key>", tmp1));
        var tmp2 = new String[1];
        tmp2[0] = "s";
        register(new CommandManager.5(this, "set", "Change a setting value", ".set <module> <setting> <value>", tmp2));
        var tmp3 = new String[1];
        tmp3[0] = "mods";
        register(new CommandManager.6(this, "modules", "List modules", ".modules [category]", tmp3));
        var tmp4 = new String[1];
        tmp4[0] = "f";
        register(new CommandManager.7(this, "friends", "Manage friend list", ".friends add/remove/list <name>", tmp4));
        var tmp5 = new String[1];
        tmp5[0] = "p";
        register(new CommandManager.8(this, "protect", "Manage protected name list", ".protect add/remove/list/clear <name>", tmp5));
        var tmp6 = new String[1];
        tmp6[0] = "h";
        register(new CommandManager.9(this, "help", "Show help", ".help [command]", tmp6));
        register(new CommandManager.10(this, "say", "Send real chat message", ".say <message>", new String[0]));
        var tmp7 = new String[2];
        tmp7[0] = "clickgui";
        tmp7[1] = "cgui";
        register(new CommandManager.11(this, "gui", "Open ClickGUI", ".gui", tmp7));
    }

    public boolean dispatch(String message) {
        if (!(message.startsWith("."))) return false;
        String stripped = message.substring(".".length()).trim();
        if (stripped.isEmpty()) return false;
        String[] args = stripped.split("\\s+");
        Object var4 = this.commands.iterator();
        while (var4.hasNext()) {
            Command cmd = ((Command) var4.next());
            if (cmd.matches(args[0])) {
                cmd.execute(args);
                return true;
            }
        }
        if (mc.field_1724 != null) {
            mc.field_1724.method_7353(class_2561.method_43470(("\u00a7d[VC]\u00a7c Unknown command: " + args[0] + ". Type .help")), false);
        }
        return true;
    }

    private void register(Command command) {
        this.commands.add(command);
    }

    private void setSettingValue(Setting setting, String value) {
        Object current = setting.get();
        if ((current instanceof Boolean)) {
            setting.set(Boolean.parseBoolean(value));
            break label_11;
        }
        if ((current instanceof Integer)) {
            setting.set(Integer.parseInt(value));
            break label_11;
        }
        if ((current instanceof Double)) {
            setting.set(Double.parseDouble(value));
            break label_11;
        }
        if ((current instanceof Float)) {
            setting.set(Float.parseFloat(value));
            break label_11;
        }
        if (!((current instanceof String))) break label_11;
        setting.set(value);
        label_11:
    }

    public List getCommands() {
        return this.commands;
    }

    static {
        mc = class_310.method_1551();
    }


    class 9 extends Command {

        9(CommandManager this$0, String arg0, String arg1, String arg2, String... arg3) {
            this.this$0 = this$0;
            super(arg0, arg1, arg2, arg3);
        }

        public void execute(String[] args) {
            if (args.length >= 2) {
                Object var2 = this.this$0.commands.iterator();
                while (var2.hasNext()) {
                    Command cmd = ((Command) var2.next());
                    if (cmd.matches(args[1])) {
                        info((cmd.getName() + " \u2014 " + cmd.getDescription()));
                        info(("Syntax: " + cmd.getSyntax()));
                        return;
                    }
                }
                error(("Unknown command: " + args[1]));
                break label_14;
            }
            info("\u00a7d--- Vie Cheats Commands ---");
            var2 = this.this$0.commands.iterator();
            while (var2.hasNext()) {
                cmd = ((Command) var2.next());
                info(("\u00a7f" + cmd.getSyntax() + " \u00a77\u2014 " + cmd.getDescription()));
            }
            label_14:
        }

    }

    class 8 extends Command {
        8(CommandManager this$0, String arg0, String arg1, String arg2, String... arg3) {
            super(arg0, arg1, arg2, arg3);
        }

        public void execute(String[] args) {
            if (args.length < 2) {
                info(".protect add <name>   \u2014 hide a name");
                info(".protect remove <name> \u2014 unhide a name");
                info(".protect list          \u2014 show all hidden names");
                info(".protect clear         \u2014 clear all");
                return;
            }
            NameProtect np = NameProtect.get();
            Object var3 = args[1].toLowerCase();
            int var4 = -1;
            switch (var3.hashCode()) {
                case -934610812 -> { break label_9; }
                case 96417 -> { break label_8; }
                case 3322014 -> { break label_10; }
                case 94746189 -> { break label_11; }
                default -> { break label_12; }
            }
            label_8:
            if (!(var3.equals("add"))) break label_12;
            var4 = 0;
            break label_12;
            label_9:
            if (!(var3.equals("remove"))) break label_12;
            var4 = 1;
            break label_12;
            label_10:
            if (!(var3.equals("list"))) break label_12;
            var4 = 2;
            break label_12;
            label_11:
            if (!(var3.equals("clear"))) break label_12;
            var4 = 3;
            label_12:
            switch (var4) {
                case 0 -> { break label_13; }
                case 1 -> { break label_29; }
                case 2 -> { break label_45; }
                case 3 -> { break label_54; }
                default -> { break label_57; }
            }
            label_13:
            if (args.length < 3) {
                error("Usage: .protect add <name>");
                return;
            }
            String name = args[2].trim();
            if (name.isBlank()) {
                error("Name cannot be blank.");
                return;
            }
            String[] current = np.getProtectedNames();
            String[] updated = current;
            int j = updated.length;
            boolean found = false;
            while (found < j) {
                String n = updated[found];
                if (n.equalsIgnoreCase(name)) {
                    error(("Name already protected: " + name));
                    return;
                }
                found++;
            }
            updated = new String[(current.length + 1)];
            System.arraycopy(current, 0, updated, 0, current.length);
            updated[current.length] = name.toLowerCase();
            np.setProtectedNames(updated);
            info(("Added to protection: " + name));
            break label_58;
            label_29:
            if (args.length < 3) {
                error("Usage: .protect remove <name>");
                return;
            }
            name = args[2].trim().toLowerCase();
            current = np.getProtectedNames();
            updated = new String[Math.max(0, (current.length - 1))];
            j = 0;
            found = false;
            n = current;
            int var11 = n.length;
            int var12 = 0;
            while (var12 < var11) {
                n = n[var12];
                if (!(n.equalsIgnoreCase(name))) {
                    j++;
                    updated[j] = n;
                } else {
                    found = true;
                }
                var12++;
            }
            if (!found) {
                error(("Name not in protection list: " + name));
                return;
            }
            np.setProtectedNames(updated);
            info(("Removed from protection: " + name));
            break label_58;
            label_45:
            String[] names = np.getProtectedNames();
            if (names.length == 0) {
                info("No names protected.");
                return;
            }
            info("\u00a7d--- Protected Names ---");
            current = names;
            updated = current.length;
            j = 0;
            while (j < updated) {
                n = current[j];
                info(("\u00a77  " + n));
                j++;
            }
            info(("Total: " + names.length));
            break label_58;
            label_54:
            np.setProtectedNames(new String[0]);
            info("Protection list cleared.");
            break label_58;
            label_57:
            error(("Unknown sub-command: " + args[1] + " (add/remove/list/clear)"));
            label_58:
        }

    }

    class 5 extends Command {

        5(CommandManager this$0, String arg0, String arg1, String arg2, String... arg3) {
            this.this$0 = this$0;
            super(arg0, arg1, arg2, arg3);
        }

        public void execute(String[] args) {
            if (args.length < 4) {
                error("Usage: .set <module> <setting> <value>");
                return;
            }
            Module mod = VieCheatsMod.getInstance().getModuleManager().getModuleByName(args[1]);
            if (mod == null) {
                error(("Module not found: " + args[1]));
                return;
            }
            Setting setting = null;
            Object var4 = mod.getSettings().iterator();
            while (var4.hasNext()) {
                Setting s = ((Setting) var4.next());
                if (s.getName().equalsIgnoreCase(args[2])) {
                    setting = s;
                    break label_8;
                }
            }
            label_8:
            if (setting == null) {
                error(("Setting not found: " + args[2]));
                return;
            }
            this.this$0.setSettingValue(setting, args[3]);
            info((mod.getName() + "." + setting.getName() + " = " + String.valueOf(setting.get())));
        }

    }

    class 6 extends Command {
        6(CommandManager this$0, String arg0, String arg1, String arg2, String... arg3) {
            super(arg0, arg1, arg2, arg3);
        }

        public void execute(String[] args) {
            if (args.length < 2) break label_12;
            try {
                Category cat = Category.valueOf(args[1].toUpperCase());
                Object var3 = VieCheatsMod.getInstance().getModuleManager().getModules(cat).iterator();
                while (var3.hasNext()) {
                    Module mod = ((Module) var3.next());
                    info(((mod.isEnabled() ? "\u00a7a" : "\u00a77") + mod.getName()));
                }
            } catch (IllegalArgumentException ex9) {
                IllegalArgumentException e = ex9;
                error(("Unknown category: " + args[1]));
                break label_22;
                label_12:
                cat = Category.values();
                var3 = cat.length;
                mod = 0;
                while (mod < var3) {
                    cat = cat[mod];
                    info(("\u00a7d--- " + cat.getDisplayName() + " ---"));
                    Object var6 = VieCheatsMod.getInstance().getModuleManager().getModules(cat).iterator();
                    while (var6.hasNext()) {
                        mod = ((Module) var6.next());
                        info(((mod.isEnabled() ? "\u00a7a" : "\u00a77") + "  " + mod.getName()));
                    }
                    mod++;
                }
            }
            label_22:
        }

    }

    class 11 extends Command {
        11(CommandManager this$0, String arg0, String arg1, String arg2, String... arg3) {
            super(arg0, arg1, arg2, arg3);
        }

        public void execute(String[] args) {
            if (mc == null) return;
            if (mc.field_1724 != null) {
                if (mc.field_1687 == null) {
                }
                error("Join a world first.");
                return;
            }
            mc.method_1507(new ClickGUI());
            info("Opening ClickGUI...");
        }

    }

    class 2 extends Command {
        2(CommandManager this$0, String arg0, String arg1, String arg2, String... arg3) {
            super(arg0, arg1, arg2, arg3);
        }

        public void execute(String[] args) {
            AuthManager auth = VieCheatsMod.getInstance().getAuthManager();
            if (auth == null) {
                info("Auth is disabled in this build.");
                return;
            }
            if (args.length < 2) {
                error("Usage: .auth check | .auth status");
                return;
            }
            Object var3 = args[1].toLowerCase();
            int var4 = -1;
            switch (var3.hashCode()) {
                case -892481550 -> { break label_9; }
                case 113762 -> { break label_10; }
                case 94627080 -> { break label_8; }
                case 94746189 -> { break label_11; }
                default -> { break label_12; }
            }
            label_8:
            if (!(var3.equals("check"))) break label_12;
            var4 = 0;
            break label_12;
            label_9:
            if (!(var3.equals("status"))) break label_12;
            var4 = 1;
            break label_12;
            label_10:
            if (!(var3.equals("set"))) break label_12;
            var4 = 2;
            break label_12;
            label_11:
            if (!(var3.equals("clear"))) break label_12;
            var4 = 3;
            label_12:
            switch (var4) {
                case 0 -> { break label_13; }
                case 1 -> { break label_16; }
                case 2 -> { break label_17; }
                case 3 -> { break label_17; }
                default -> { break label_18; }
            }
            label_13:
            AuthManager.StatusSnapshot snapshot = auth.validateStoredKey();
            printAuthSnapshot(snapshot);
            break label_19;
            label_16:
            printAuthSnapshot(auth.getStatusSnapshot());
            break label_19;
            label_17:
            error("Manual key command is disabled. Manage access via Discord redeem/download + HWID reset.");
            break label_19;
            label_18:
            error(("Unknown sub-command: " + args[1]));
            label_19:
        }

        private void printAuthSnapshot(AuthManager.StatusSnapshot snapshot) {
            String color = (snapshot.authorized() ? "\u00a7a" : "\u00a7c");
            info(("Auth: " + color + snapshot.status().toUpperCase()));
            info(("Server: " + snapshot.apiBaseUrl()));
            info(("Key: " + maskKey(snapshot.keyCode())));
            info(("HWID: " + shortHwid(snapshot.hwid())));
            if (snapshot.expiresAt() != null) {
                info(("Expires: " + String.valueOf(snapshot.expiresAt())));
            } else {
                info("Expires: Lifetime/Unknown");
            }
            info(("Message: " + snapshot.message()));
            if (!(snapshot.authorized()) && !(!"server_error".equalsIgnoreCase(snapshot.status()))) {
                info("Hint: check auth API URL and /health endpoint.");
            }
        }

        private String maskKey(String key) {
            if ((key == null) || (key.isBlank())) {
                return "(none)";
            }
            if (key.length() <= 10) return key;
            return (key.substring(0, 6) + "..." + key.substring((key.length() - 4)));
        }

        private String shortHwid(String hwid) {
            if ((hwid == null) || (hwid.isBlank())) {
                return "(unknown)";
            }
            if (hwid.length() <= 16) return hwid;
            return (hwid.substring(0, 16) + "...");
        }

    }

    class 10 extends Command {
        10(CommandManager this$0, String arg0, String arg1, String arg2, String... arg3) {
            super(arg0, arg1, arg2, arg3);
        }

        public void execute(String[] args) {
            if (args.length < 2) {
                error("Usage: .say <message>");
                return;
            }
            String msg = String.join(" ", ((CharSequence[]) Arrays.copyOfRange(args, 1, args.length)));
            if (mc.field_1724 != null) {
                mc.field_1724.field_3944.method_45729(msg);
            }
        }

    }

    class 1 extends Command {
        1(CommandManager this$0, String arg0, String arg1, String arg2, String... arg3) {
            super(arg0, arg1, arg2, arg3);
        }

        public void execute(String[] args) {
            info("Vie Cheats v1.0.0 by Drake");
            info(("Modules: " + VieCheatsMod.getInstance().getModuleManager().getModules().size()));
            info(("Enabled: " + VieCheatsMod.getInstance().getModuleManager().getEnabled().size()));
            AuthManager auth = VieCheatsMod.getInstance().getAuthManager();
            if (auth != null) {
                AuthManager.StatusSnapshot snapshot = auth.getStatusSnapshot();
                if (snapshot.authorized()) {
                } else {
                }
                "\u00a7aVALID".info(("Auth: " + ("\u00a7c" + snapshot.status().toUpperCase())));
            } else {
                info("Auth: \u00a77DISABLED");
            }
        }

    }

    class 3 extends Command {
        3(CommandManager this$0, String arg0, String arg1, String arg2, String... arg3) {
            super(arg0, arg1, arg2, arg3);
        }

        public void execute(String[] args) {
            if (args.length < 2) {
                error("Usage: .toggle <module>");
                return;
            }
            Module mod = VieCheatsMod.getInstance().getModuleManager().getModuleByName(args[1]);
            if (mod == null) {
                error(("Module not found: " + args[1]));
                return;
            }
            mod.toggle();
            info((mod.getName() + " -> " + (mod.isEnabled() ? "\u00a7aON" : "\u00a7cOFF")));
        }

    }

    class 7 extends Command {
        7(CommandManager this$0, String arg0, String arg1, String arg2, String... arg3) {
            super(arg0, arg1, arg2, arg3);
        }

        public void execute(String[] args) {
            if (args.length >= 2) break label_1;
            error("Usage: .friends add/remove/list <name>");
            return;
            label_1:
            VieCheatsMod.FriendManager fm = VieCheatsMod.getInstance().getFriendManager();
            Object var3 = args[1].toLowerCase();
            int var4 = -1;
            switch (var3.hashCode()) {
                case -934610812 -> { break label_4; }
                case 96417 -> { break label_3; }
                case 3322014 -> { break label_5; }
                default -> { break label_6; }
            }
            label_3:
            if (!(var3.equals("add"))) break label_6;
            var4 = 0;
            break label_6;
            label_4:
            if (!(var3.equals("remove"))) break label_6;
            var4 = 1;
            break label_6;
            label_5:
            if (!(var3.equals("list"))) break label_6;
            var4 = 2;
            label_6:
            switch (var4) {
                case 0 -> { break label_7; }
                case 1 -> { break label_11; }
                case 2 -> { break label_15; }
                default -> { break label_19; }
            }
            label_7:
            if (args.length < 3) {
                error("Usage: .friends add <name>");
                return;
            }
            fm.add(args[2]);
            info(("Added friend: " + args[2]));
            break label_20;
            label_11:
            if (args.length < 3) {
                error("Usage: .friends remove <name>");
                return;
            }
            fm.remove(args[2]);
            info(("Removed friend: " + args[2]));
            break label_20;
            label_15:
            List friends = fm.getFriends();
            if (friends.isEmpty()) {
                info("No friends added.");
                return;
            }
            info(("Friends: " + String.join(", ", friends)));
            break label_20;
            label_19:
            error(("Unknown sub-command: " + args[1]));
            label_20:
        }

    }

    class 4 extends Command {
        4(CommandManager this$0, String arg0, String arg1, String arg2, String... arg3) {
            super(arg0, arg1, arg2, arg3);
        }

        public void execute(String[] args) {
            if (args.length < 3) {
                error("Usage: .bind <module> <key>");
                return;
            }
            Module mod = VieCheatsMod.getInstance().getModuleManager().getModuleByName(args[1]);
            if (mod == null) {
                error(("Module not found: " + args[1]));
                return;
                try {
                }
                int keyCode = Integer.parseInt(args[2]);
                mod.getKeybind().set(keyCode);
                info(("Bound " + mod.getName() + " to key " + keyCode));
            } catch (NumberFormatException ex7) {
                NumberFormatException e = ex7;
                error(("Invalid key code: " + args[2]));
            }
        }

    }
}
