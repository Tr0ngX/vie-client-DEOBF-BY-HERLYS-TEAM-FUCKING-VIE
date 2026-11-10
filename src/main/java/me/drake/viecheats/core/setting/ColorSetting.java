package me.drake.viecheats.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ColorSetting extends Setting {
    public ColorSetting(String name, String description, int defaultColor) {
        super(name, description, defaultColor);
    }

    public int getAlpha() {
        return ((((Integer) get()).intValue() >> 24) & 255);
    }

    public int getRed() {
        return ((((Integer) get()).intValue() >> 16) & 255);
    }

    public int getGreen() {
        return ((((Integer) get()).intValue() >> 8) & 255);
    }

    public int getBlue() {
        return (((Integer) get()).intValue() & 255);
    }

    public void setRGBA(int r, int g, int b, int a) {
        set((((((a & 255) << 24) | ((r & 255) << 16)) | ((g & 255) << 8)) | (b & 255)));
    }

    public float[] asFloatArray() {
        var tmp0 = new float[4];
        tmp0[0] = (((float) getRed()) / 255.0f);
        tmp0[1] = (((float) getGreen()) / 255.0f);
        tmp0[2] = (((float) getBlue()) / 255.0f);
        tmp0[3] = (((float) getAlpha()) / 255.0f);
        return tmp0;
    }

    public JsonElement toJson() {
        return new JsonPrimitive(Integer.toHexString(((Integer) get()).intValue()));
    }

    public void fromJson(JsonElement json) {
        if ((json == null) || (!(json.isJsonPrimitive()))) break label_4;
        try {
            set(((int) Long.parseLong(json.getAsString(), 16)));
        } catch (NumberFormatException ex3) {
            Object var2 = ex3;
        }
        label_4:
    }

}
