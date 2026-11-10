package me.drake.viecheats.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class DoubleSetting extends Setting {
    private final double min;
    private final double max;
    private final double step;

    public DoubleSetting(String name, String description, double defaultValue, double min, double max) {
        this(name, description, defaultValue, min, max, 0.1);
    }

    public DoubleSetting(String name, String description, double defaultValue, double min, double max, double step) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public void set(Double value) {
        double clamped = Math.max(this.min, Math.min(this.max, value.doubleValue()));
        if (this.step > 0.0) {
            clamped = (((double) Math.round((clamped / this.step))) * this.step);
        }
        super.set(clamped);
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

    public double getStep() {
        return this.step;
    }

    public JsonElement toJson() {
        return new JsonPrimitive(((Number) get()));
    }

    public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) {
            set(json.getAsDouble());
        }
    }

}
