package me.drake.viecheats.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class EnumSetting extends Setting {
    private final Class enumClass;

    public EnumSetting(String name, String description, Enum defaultValue) {
        super(name, description, defaultValue);
        this.enumClass = defaultValue.getDeclaringClass();
    }

    public EnumSetting(String name, String description, Enum defaultValue, Enum[] values) {
        this(name, description, defaultValue);
    }

    public void cycle() {
        cycle(false);
    }

    public void cycle(boolean reverse) {
        Enum[] constants = ((Enum[]) this.enumClass.getEnumConstants());
        int delta = (!(!reverse) ? -1 : 1);
        int next = (((((Enum) get()).ordinal() + delta) + constants.length) % constants.length);
        set(constants[next]);
    }

    public Enum[] getValues() {
        return ((Enum[]) this.enumClass.getEnumConstants());
    }

    public Class getEnumClass() {
        return this.enumClass;
    }

    public JsonElement toJson() {
        return new JsonPrimitive(((Enum) get()).name());
    }

    public void fromJson(JsonElement json) {
        if ((json == null) || (!(json.isJsonPrimitive()))) break label_4;
        try {
            set(Enum.valueOf(this.enumClass, json.getAsString()));
        } catch (IllegalArgumentException ex3) {
            Object var2 = ex3;
        }
        label_4:
    }

}
