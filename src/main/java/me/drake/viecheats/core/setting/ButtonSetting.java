package me.drake.viecheats.core.setting;

import com.google.gson.JsonElement;

public class ButtonSetting extends Setting {
    private final Runnable action;

    public ButtonSetting(String name, String description, Runnable action) {
        super(name, description, false);
        this.action = action;
    }

    public void press() {
        if (this.action != null) {
            this.action.run();
        }
    }

    public JsonElement toJson() {
        return null;
    }

    public void fromJson(JsonElement json) {
    }

}
