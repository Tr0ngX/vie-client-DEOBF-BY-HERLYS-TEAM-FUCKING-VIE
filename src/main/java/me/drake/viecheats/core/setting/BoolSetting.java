package me.drake.viecheats.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BoolSetting extends Setting {
    public BoolSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public boolean isEnabled() {
        return ((Boolean) get()).booleanValue();
    }

    public void toggle() {
        set(!(((Boolean) get()).booleanValue()));
    }

    public JsonElement toJson() {
        return new JsonPrimitive(((Boolean) get()));
    }

    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            set(json.getAsBoolean());
        }
    }

}
