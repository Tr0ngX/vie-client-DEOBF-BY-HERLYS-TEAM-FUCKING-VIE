package me.drake.viecheats.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.Consumer;

public class Setting {
    private final String name;
    private final String description;
    private final Object defaultValue;
    private Object value;
    private boolean visibleInGui;
    private Consumer onChanged;

    public Setting(String name, String description, Object defaultValue) {
        this.visibleInGui = true;
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public Object get() {
        return this.value;
    }

    public void set(Object value) {
        Object old = this.value;
        this.value = value;
        if (this.onChanged != null && !(value.equals(old))) {
            this.onChanged.accept(value);
        }
    }

    public void reset() {
        set(this.defaultValue);
    }

    public Setting onChanged(Consumer callback) {
        this.onChanged = callback;
        return this;
    }

    public boolean isVisibleInGui() {
        return this.visibleInGui;
    }

    public Setting setVisibleInGui(boolean visibleInGui) {
        this.visibleInGui = visibleInGui;
        return this;
    }

    public JsonElement toJson() {
        Object var2 = this.value;
        if ((var2 instanceof Boolean)) {
            Boolean b = ((Boolean) var2);
            return new JsonPrimitive(b);
        }
        var2 = this.value;
        if ((var2 instanceof Number)) {
            Number n = ((Number) var2);
            return new JsonPrimitive(n);
        }
        var2 = this.value;
        if ((var2 instanceof String)) {
            String s = ((String) var2);
            return new JsonPrimitive(s);
        }
        return new JsonPrimitive(this.value.toString());
    }

    public void fromJson(JsonElement json) {
        if (json == null) break label_1;
        if (json.isJsonNull()) {
            label_1:
            return;
            try {
            }
            if ((this.value instanceof Boolean)) {
                set(json.getAsBoolean());
                break label_12;
            }
            if ((this.value instanceof Integer)) {
                set(json.getAsInt());
                break label_12;
            }
            if ((this.value instanceof Double)) {
                set(json.getAsDouble());
                break label_12;
            }
            if ((this.value instanceof Float)) {
                set(json.getAsFloat());
                break label_12;
            }
            if (!((this.value instanceof String))) break label_12;
            set(json.getAsString());
            label_12:
        } catch (Exception ex13) {
            Object var2 = ex13;
        }
    }

}
