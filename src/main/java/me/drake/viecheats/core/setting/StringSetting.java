package me.drake.viecheats.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringSetting extends Setting {
    public StringSetting(String name, String description, String defaultValue) {
        super(name, description, defaultValue);
    }

    public JsonElement toJson() {
        return new JsonPrimitive(((String) get()));
    }

    public void fromJson(JsonElement j) {
        if (j != null && j.isJsonPrimitive()) {
            set(j.getAsString());
        }
    }

}
