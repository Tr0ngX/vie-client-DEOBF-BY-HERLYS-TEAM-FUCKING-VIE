package me.drake.viecheats.core.command;

import net.minecraft.class_2561;
import net.minecraft.class_310;

public abstract class Command {
    private final String name;
    private final String description;
    private final String syntax;
    private final String[] aliases;
    protected static final class_310 mc;

    protected Command(String name, String description, String syntax, String... aliases) {
        this.name = name;
        this.description = description;
        this.syntax = syntax;
        this.aliases = aliases;
    }

    public abstract void execute(String[] arg0);

    protected void info(String message) {
        if (mc.field_1724 != null) {
            mc.field_1724.method_7353(class_2561.method_43470(("\u00a7d[VC]\u00a7r " + message)), false);
        }
    }

    protected void error(String message) {
        if (mc.field_1724 != null) {
            mc.field_1724.method_7353(class_2561.method_43470(("\u00a7d[VC]\u00a7c " + message)), false);
        }
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getSyntax() {
        return this.syntax;
    }

    public String[] getAliases() {
        return this.aliases;
    }

    public boolean matches(String input) {
        if (this.name.equalsIgnoreCase(input)) return true;
        Object var2 = this.aliases;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            String alias = var2[var4];
            if (alias.equalsIgnoreCase(input)) return true;
            var4++;
        }
        return false;
    }

    static {
        mc = class_310.method_1551();
    }

}
