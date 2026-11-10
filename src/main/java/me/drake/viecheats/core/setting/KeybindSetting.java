package me.drake.viecheats.core.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class KeybindSetting extends Setting {
    public KeybindSetting(String name, String description, int defaultKey) {
        super(name, description, defaultKey);
    }

    public KeybindSetting(String name, String description) {
        this(name, description, -1);
    }

    public boolean isBound() {
        return ((Integer) get()).intValue() != -1;
    }

    public String getKeyName() {
        if (!(isBound())) return "NONE";
        if (((Integer) get()).intValue() >= 1000) {
            int mb = (((Integer) get()).intValue() - 1000);
            if (mb == 0) return "MB1 (Left)";
            if (mb == 1) return "MB2 (Right)";
            if (mb == 2) return "MB3 (Mid)";
            return ("MB" + (mb + 1));
        }
        return keyCodeToName(((Integer) get()).intValue());
    }

    private static String keyCodeToName(int k) {
        if (k == 0) break label_1;
        if (k != -1) break label_2;
        label_1:
        return "NONE";
        label_2:
        if (k < 0) return ("MB" + -(k));
        switch (k) {
            case 32 -> { break label_74; }
            case 33 -> { break label_107; }
            case 34 -> { break label_107; }
            case 35 -> { break label_107; }
            case 36 -> { break label_107; }
            case 37 -> { break label_107; }
            case 38 -> { break label_107; }
            case 39 -> { break label_54; }
            case 40 -> { break label_107; }
            case 41 -> { break label_107; }
            case 42 -> { break label_107; }
            case 43 -> { break label_107; }
            case 44 -> { break label_62; }
            case 45 -> { break label_28; }
            case 46 -> { break label_63; }
            case 47 -> { break label_64; }
            case 48 -> { break label_27; }
            case 49 -> { break label_18; }
            case 50 -> { break label_19; }
            case 51 -> { break label_20; }
            case 52 -> { break label_21; }
            case 53 -> { break label_22; }
            case 54 -> { break label_23; }
            case 55 -> { break label_24; }
            case 56 -> { break label_25; }
            case 57 -> { break label_26; }
            case 58 -> { break label_107; }
            case 59 -> { break label_53; }
            case 60 -> { break label_107; }
            case 61 -> { break label_29; }
            case 62 -> { break label_107; }
            case 63 -> { break label_107; }
            case 64 -> { break label_107; }
            case 65 -> { break label_44; }
            case 66 -> { break label_59; }
            case 67 -> { break label_57; }
            case 68 -> { break label_46; }
            case 69 -> { break label_32; }
            case 70 -> { break label_47; }
            case 71 -> { break label_48; }
            case 72 -> { break label_49; }
            case 73 -> { break label_37; }
            case 74 -> { break label_50; }
            case 75 -> { break label_51; }
            case 76 -> { break label_52; }
            case 77 -> { break label_61; }
            case 78 -> { break label_60; }
            case 79 -> { break label_38; }
            case 80 -> { break label_39; }
            case 81 -> { break label_30; }
            case 82 -> { break label_33; }
            case 83 -> { break label_45; }
            case 84 -> { break label_34; }
            case 85 -> { break label_36; }
            case 86 -> { break label_58; }
            case 87 -> { break label_31; }
            case 88 -> { break label_56; }
            case 89 -> { break label_35; }
            case 90 -> { break label_55; }
            case 91 -> { break label_40; }
            case 92 -> { break label_42; }
            case 93 -> { break label_41; }
            case 94 -> { break label_107; }
            case 95 -> { break label_107; }
            case 96 -> { break label_17; }
            case 97 -> { break label_107; }
            case 98 -> { break label_107; }
            case 99 -> { break label_107; }
            case 100 -> { break label_107; }
            case 101 -> { break label_107; }
            case 102 -> { break label_107; }
            case 103 -> { break label_107; }
            case 104 -> { break label_107; }
            case 105 -> { break label_107; }
            case 106 -> { break label_107; }
            case 107 -> { break label_107; }
            case 108 -> { break label_107; }
            case 109 -> { break label_107; }
            case 110 -> { break label_107; }
            case 111 -> { break label_107; }
            case 112 -> { break label_107; }
            case 113 -> { break label_107; }
            case 114 -> { break label_107; }
            case 115 -> { break label_107; }
            case 116 -> { break label_107; }
            case 117 -> { break label_107; }
            case 118 -> { break label_107; }
            case 119 -> { break label_107; }
            case 120 -> { break label_107; }
            case 121 -> { break label_107; }
            case 122 -> { break label_107; }
            case 123 -> { break label_107; }
            case 124 -> { break label_107; }
            case 125 -> { break label_107; }
            case 126 -> { break label_107; }
            case 127 -> { break label_107; }
            case 128 -> { break label_107; }
            case 129 -> { break label_107; }
            case 130 -> { break label_107; }
            case 131 -> { break label_107; }
            case 132 -> { break label_107; }
            case 133 -> { break label_107; }
            case 134 -> { break label_107; }
            case 135 -> { break label_107; }
            case 136 -> { break label_107; }
            case 137 -> { break label_107; }
            case 138 -> { break label_107; }
            case 139 -> { break label_107; }
            case 140 -> { break label_107; }
            case 141 -> { break label_107; }
            case 142 -> { break label_107; }
            case 143 -> { break label_107; }
            case 144 -> { break label_107; }
            case 145 -> { break label_107; }
            case 146 -> { break label_107; }
            case 147 -> { break label_107; }
            case 148 -> { break label_107; }
            case 149 -> { break label_107; }
            case 150 -> { break label_107; }
            case 151 -> { break label_107; }
            case 152 -> { break label_107; }
            case 153 -> { break label_107; }
            case 154 -> { break label_107; }
            case 155 -> { break label_107; }
            case 156 -> { break label_107; }
            case 157 -> { break label_107; }
            case 158 -> { break label_107; }
            case 159 -> { break label_107; }
            case 160 -> { break label_107; }
            case 161 -> { break label_107; }
            case 162 -> { break label_107; }
            case 163 -> { break label_107; }
            case 164 -> { break label_107; }
            case 165 -> { break label_107; }
            case 166 -> { break label_107; }
            case 167 -> { break label_107; }
            case 168 -> { break label_107; }
            case 169 -> { break label_107; }
            case 170 -> { break label_107; }
            case 171 -> { break label_107; }
            case 172 -> { break label_107; }
            case 173 -> { break label_107; }
            case 174 -> { break label_107; }
            case 175 -> { break label_107; }
            case 176 -> { break label_107; }
            case 177 -> { break label_107; }
            case 178 -> { break label_107; }
            case 179 -> { break label_107; }
            case 180 -> { break label_107; }
            case 181 -> { break label_107; }
            case 182 -> { break label_107; }
            case 183 -> { break label_107; }
            case 184 -> { break label_107; }
            case 185 -> { break label_107; }
            case 186 -> { break label_107; }
            case 187 -> { break label_107; }
            case 188 -> { break label_107; }
            case 189 -> { break label_107; }
            case 190 -> { break label_107; }
            case 191 -> { break label_107; }
            case 192 -> { break label_107; }
            case 193 -> { break label_107; }
            case 194 -> { break label_107; }
            case 195 -> { break label_107; }
            case 196 -> { break label_107; }
            case 197 -> { break label_107; }
            case 198 -> { break label_107; }
            case 199 -> { break label_107; }
            case 200 -> { break label_107; }
            case 201 -> { break label_107; }
            case 202 -> { break label_107; }
            case 203 -> { break label_107; }
            case 204 -> { break label_107; }
            case 205 -> { break label_107; }
            case 206 -> { break label_107; }
            case 207 -> { break label_107; }
            case 208 -> { break label_107; }
            case 209 -> { break label_107; }
            case 210 -> { break label_107; }
            case 211 -> { break label_107; }
            case 212 -> { break label_107; }
            case 213 -> { break label_107; }
            case 214 -> { break label_107; }
            case 215 -> { break label_107; }
            case 216 -> { break label_107; }
            case 217 -> { break label_107; }
            case 218 -> { break label_107; }
            case 219 -> { break label_107; }
            case 220 -> { break label_107; }
            case 221 -> { break label_107; }
            case 222 -> { break label_107; }
            case 223 -> { break label_107; }
            case 224 -> { break label_107; }
            case 225 -> { break label_107; }
            case 226 -> { break label_107; }
            case 227 -> { break label_107; }
            case 228 -> { break label_107; }
            case 229 -> { break label_107; }
            case 230 -> { break label_107; }
            case 231 -> { break label_107; }
            case 232 -> { break label_107; }
            case 233 -> { break label_107; }
            case 234 -> { break label_107; }
            case 235 -> { break label_107; }
            case 236 -> { break label_107; }
            case 237 -> { break label_107; }
            case 238 -> { break label_107; }
            case 239 -> { break label_107; }
            case 240 -> { break label_107; }
            case 241 -> { break label_107; }
            case 242 -> { break label_107; }
            case 243 -> { break label_107; }
            case 244 -> { break label_107; }
            case 245 -> { break label_107; }
            case 246 -> { break label_107; }
            case 247 -> { break label_107; }
            case 248 -> { break label_107; }
            case 249 -> { break label_107; }
            case 250 -> { break label_107; }
            case 251 -> { break label_107; }
            case 252 -> { break label_107; }
            case 253 -> { break label_107; }
            case 254 -> { break label_107; }
            case 255 -> { break label_107; }
            case 256 -> { break label_4; }
            case 257 -> { break label_77; }
            case 258 -> { break label_76; }
            case 259 -> { break label_75; }
            case 260 -> { break label_82; }
            case 261 -> { break label_83; }
            case 262 -> { break label_81; }
            case 263 -> { break label_80; }
            case 264 -> { break label_79; }
            case 265 -> { break label_78; }
            case 266 -> { break label_86; }
            case 267 -> { break label_87; }
            case 268 -> { break label_84; }
            case 269 -> { break label_85; }
            case 270 -> { break label_107; }
            case 271 -> { break label_107; }
            case 272 -> { break label_107; }
            case 273 -> { break label_107; }
            case 274 -> { break label_107; }
            case 275 -> { break label_107; }
            case 276 -> { break label_107; }
            case 277 -> { break label_107; }
            case 278 -> { break label_107; }
            case 279 -> { break label_107; }
            case 280 -> { break label_43; }
            case 281 -> { break label_106; }
            case 282 -> { break label_88; }
            case 283 -> { break label_105; }
            case 284 -> { break label_107; }
            case 285 -> { break label_107; }
            case 286 -> { break label_107; }
            case 287 -> { break label_107; }
            case 288 -> { break label_107; }
            case 289 -> { break label_107; }
            case 290 -> { break label_5; }
            case 291 -> { break label_6; }
            case 292 -> { break label_7; }
            case 293 -> { break label_8; }
            case 294 -> { break label_9; }
            case 295 -> { break label_10; }
            case 296 -> { break label_11; }
            case 297 -> { break label_12; }
            case 298 -> { break label_13; }
            case 299 -> { break label_14; }
            case 300 -> { break label_15; }
            case 301 -> { break label_16; }
            case 302 -> { break label_107; }
            case 303 -> { break label_107; }
            case 304 -> { break label_107; }
            case 305 -> { break label_107; }
            case 306 -> { break label_107; }
            case 307 -> { break label_107; }
            case 308 -> { break label_107; }
            case 309 -> { break label_107; }
            case 310 -> { break label_107; }
            case 311 -> { break label_107; }
            case 312 -> { break label_107; }
            case 313 -> { break label_107; }
            case 314 -> { break label_107; }
            case 315 -> { break label_107; }
            case 316 -> { break label_107; }
            case 317 -> { break label_107; }
            case 318 -> { break label_107; }
            case 319 -> { break label_107; }
            case 320 -> { break label_89; }
            case 321 -> { break label_101; }
            case 322 -> { break label_102; }
            case 323 -> { break label_103; }
            case 324 -> { break label_98; }
            case 325 -> { break label_99; }
            case 326 -> { break label_100; }
            case 327 -> { break label_94; }
            case 328 -> { break label_95; }
            case 329 -> { break label_96; }
            case 330 -> { break label_90; }
            case 331 -> { break label_91; }
            case 332 -> { break label_92; }
            case 333 -> { break label_93; }
            case 334 -> { break label_97; }
            case 335 -> { break label_104; }
            case 336 -> { break label_107; }
            case 337 -> { break label_107; }
            case 338 -> { break label_107; }
            case 339 -> { break label_107; }
            case 340 -> { break label_65; }
            case 341 -> { break label_67; }
            case 342 -> { break label_69; }
            case 343 -> { break label_71; }
            case 344 -> { break label_66; }
            case 345 -> { break label_68; }
            case 346 -> { break label_70; }
            case 347 -> { break label_72; }
            case 348 -> { break label_73; }
            default -> { break label_107; }
        }
        label_4:
        break label_108;
        label_5:
        break label_108;
        label_6:
        break label_108;
        label_7:
        break label_108;
        label_8:
        break label_108;
        label_9:
        break label_108;
        label_10:
        break label_108;
        label_11:
        break label_108;
        label_12:
        break label_108;
        label_13:
        break label_108;
        label_14:
        break label_108;
        label_15:
        break label_108;
        label_16:
        break label_108;
        label_17:
        break label_108;
        label_18:
        break label_108;
        label_19:
        break label_108;
        label_20:
        break label_108;
        label_21:
        break label_108;
        label_22:
        break label_108;
        label_23:
        break label_108;
        label_24:
        break label_108;
        label_25:
        break label_108;
        label_26:
        break label_108;
        label_27:
        break label_108;
        label_28:
        break label_108;
        label_29:
        break label_108;
        label_30:
        break label_108;
        label_31:
        break label_108;
        label_32:
        break label_108;
        label_33:
        break label_108;
        label_34:
        break label_108;
        label_35:
        break label_108;
        label_36:
        break label_108;
        label_37:
        break label_108;
        label_38:
        break label_108;
        label_39:
        break label_108;
        label_40:
        break label_108;
        label_41:
        break label_108;
        label_42:
        break label_108;
        label_43:
        break label_108;
        label_44:
        break label_108;
        label_45:
        break label_108;
        label_46:
        break label_108;
        label_47:
        break label_108;
        label_48:
        break label_108;
        label_49:
        break label_108;
        label_50:
        break label_108;
        label_51:
        break label_108;
        label_52:
        break label_108;
        label_53:
        break label_108;
        label_54:
        break label_108;
        label_55:
        break label_108;
        label_56:
        break label_108;
        label_57:
        break label_108;
        label_58:
        break label_108;
        label_59:
        break label_108;
        label_60:
        break label_108;
        label_61:
        break label_108;
        label_62:
        break label_108;
        label_63:
        break label_108;
        label_64:
        break label_108;
        label_65:
        break label_108;
        label_66:
        break label_108;
        label_67:
        break label_108;
        label_68:
        break label_108;
        label_69:
        break label_108;
        label_70:
        break label_108;
        label_71:
        break label_108;
        label_72:
        break label_108;
        label_73:
        break label_108;
        label_74:
        break label_108;
        label_75:
        break label_108;
        label_76:
        break label_108;
        label_77:
        break label_108;
        label_78:
        break label_108;
        label_79:
        break label_108;
        label_80:
        break label_108;
        label_81:
        break label_108;
        label_82:
        break label_108;
        label_83:
        break label_108;
        label_84:
        break label_108;
        label_85:
        break label_108;
        label_86:
        break label_108;
        label_87:
        break label_108;
        label_88:
        break label_108;
        label_89:
        break label_108;
        label_90:
        break label_108;
        label_91:
        break label_108;
        label_92:
        break label_108;
        label_93:
        break label_108;
        label_94:
        break label_108;
        label_95:
        break label_108;
        label_96:
        break label_108;
        label_97:
        break label_108;
        label_98:
        break label_108;
        label_99:
        break label_108;
        label_100:
        break label_108;
        label_101:
        break label_108;
        label_102:
        break label_108;
        label_103:
        break label_108;
        label_104:
        break label_108;
        label_105:
        break label_108;
        label_106:
        break label_108;
        label_107:
        label_108:
        return ("KEY" + k);
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
