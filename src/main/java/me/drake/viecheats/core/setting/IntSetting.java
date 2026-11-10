package me.drake.viecheats.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class IntSetting extends Setting {
    private final int min;
    private final int max;
    private final int step;

    public IntSetting(String name, String description, int defaultValue, int min, int max) {
        this(name, description, defaultValue, min, max, 1);
    }

    public IntSetting(String name, String description, int defaultValue, int min, int max, int step) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public void set(Integer value) {
        int clamped = Math.max(this.min, Math.min(this.max, value.intValue()));
        if (this.step > 1) {
            clamped = ((Math.round((((float) (clamped - this.min)) / ((float) this.step))) * this.step) + this.min);
        }
        super.set(clamped);
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public int getStep() {
        return this.step;
    }

    public JsonElement toJson() {
        return new JsonPrimitive(((Number) get()));
    }

    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            set(json.getAsInt());
        }
    }

}
