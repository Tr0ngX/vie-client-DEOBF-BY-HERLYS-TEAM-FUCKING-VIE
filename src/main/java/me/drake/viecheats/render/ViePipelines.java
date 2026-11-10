package me.drake.viecheats.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.drake.viecheats.VieCheatsMod;
import net.minecraft.class_10789;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import org.apache.commons.io.IOUtils;

public abstract class ViePipelines {
    private static final List PIPELINES;
    public static boolean isPrecompiled;
    private static final String FALLBACK_POS_COLOR_VERT = "#version 330 core\n\nlayout (location = 0) in vec4 pos;\nlayout (location = 1) in vec4 color;\n\nlayout (std140) uniform MeshData {\n    mat4 u_Proj;\n    mat4 u_ModelView;\n};\n\nout vec4 v_Color;\n\nvoid main() {\n    gl_Position = u_Proj * u_ModelView * pos;\n    v_Color = color;\n}\n";
    private static final String FALLBACK_POS_COLOR_FRAG = "#version 330 core\n\nin vec4 v_Color;\nout vec4 color;\n\nvoid main() {\n    color = v_Color;\n}\n";
    private static final String FALLBACK_HUD_COLOR_VERT = "#version 330 core\n\nlayout (location = 0) in vec4 pos;\nlayout (location = 1) in vec4 color;\n\nout vec4 v_Color;\n\nvoid main() {\n    gl_Position = vec4(pos.xyz, 1.0);\n    v_Color = color;\n}\n";
    private static final String FALLBACK_HUD_COLOR_FRAG = "#version 330 core\n\nin vec4 v_Color;\nout vec4 color;\n\nvoid main() {\n    color = v_Color;\n}\n";
    private static final String FALLBACK_HUD_ROUNDED_VERT = "#version 330 core\n\nlayout (location = 0) in vec4 pos;\nlayout (location = 1) in vec2 uv;\nlayout (location = 2) in vec4 color;\n\nout vec2 v_Uv;\nout vec4 v_Color;\n\nvoid main() {\n    gl_Position = pos;\n    v_Uv = uv;\n    v_Color = color;\n}\n";
    private static final String FALLBACK_HUD_ROUNDED_FRAG = "#version 330 core\n\nlayout (std140) uniform RoundData {\n    vec4 u_RoundA;\n    vec4 u_RoundB;\n    vec4 u_RoundC;\n};\n\nin vec2 v_Uv;\nin vec4 v_Color;\nout vec4 color;\n\nfloat roundedRectSdfVarying(vec2 p, vec2 halfSize, vec4 radii) {\n    float radius = radii.x;\n    if (p.x > 0.0 && p.y < 0.0) radius = radii.y;\n    else if (p.x > 0.0 && p.y >= 0.0) radius = radii.z;\n    else if (p.x <= 0.0 && p.y >= 0.0) radius = radii.w;\n    vec2 q = abs(p) - (halfSize - vec2(radius));\n    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;\n}\n\nvoid main() {\n    float width = u_RoundA.x;\n    float height = u_RoundA.y;\n    float softness = max(0.001, u_RoundA.z);\n    float mode = u_RoundA.w;\n    float stroke = max(0.0, u_RoundB.x);\n    vec4 radii = vec4(u_RoundB.y, u_RoundB.z, u_RoundB.w, u_RoundC.x);\n\n    vec2 size = vec2(width, height);\n    vec2 p = v_Uv - size * 0.5;\n    vec2 halfSize = size * 0.5;\n    float d = roundedRectSdfVarying(p, halfSize, radii);\n\n    float fillAlpha = 1.0 - smoothstep(0.0, softness, d);\n    float alpha = fillAlpha;\n    if (mode > 0.5) {\n        float innerEdge = smoothstep(-stroke - softness, -stroke + softness, d);\n        float outerEdge = 1.0 - smoothstep(0.0, softness, d);\n        alpha = innerEdge * outerEdge;\n    }\n\n    float outAlpha = v_Color.a * alpha;\n    if (outAlpha <= 0.001) discard;\n    color = vec4(v_Color.rgb, outAlpha);\n}\n";
    private static final String FALLBACK_HUD_TEX_VERT = "#version 330 core\n\nlayout (location = 0) in vec4 pos;\nlayout (location = 1) in vec2 uv;\nlayout (location = 2) in vec4 color;\n\nout vec2 v_Uv;\nout vec4 v_Color;\n\nvoid main() {\n    gl_Position = pos;\n    v_Uv = uv;\n    v_Color = color;\n}\n";
    private static final String FALLBACK_HUD_TEX_FRAG = "#version 330 core\n\nuniform sampler2D Sampler0;\n\nin vec2 v_Uv;\nin vec4 v_Color;\nout vec4 color;\n\nvoid main() {\n    vec4 texColor = texture(Sampler0, v_Uv);\n    float outAlpha = texColor.a * v_Color.a;\n    if (outAlpha <= 0.001) discard;\n    color = vec4(texColor.rgb * v_Color.rgb, outAlpha);\n}\n";
    private static final String FALLBACK_HUD_TEXT_SDF_VERT = "#version 330 core\n\nlayout (location = 0) in vec4 pos;\nlayout (location = 1) in vec2 uv;\nlayout (location = 2) in vec4 color;\n\nout vec2 v_Uv;\nout vec4 v_Color;\n\nvoid main() {\n    gl_Position = pos;\n    v_Uv = uv;\n    v_Color = color;\n}\n";
    private static final String FALLBACK_HUD_TEXT_SDF_FRAG = "#version 330 core\n\nuniform sampler2D Sampler0;\n\nin vec2 v_Uv;\nin vec4 v_Color;\nout vec4 color;\n\nvoid main() {\n    float sdf = texture(Sampler0, v_Uv).a;\n    float edge = 192.0 / 255.0;\n    float aa = clamp(fwidth(sdf) * 0.48 + 0.0008, 0.0008, 0.028);\n    float alpha = smoothstep(edge - aa, edge + aa, sdf);\n    alpha = pow(alpha, 0.98);\n\n    float outAlpha = v_Color.a * alpha;\n    if (outAlpha <= 0.001) discard;\n    color = vec4(v_Color.rgb, outAlpha);\n}\n";
    private static final RenderPipeline.Snippet MESH_UNIFORMS;
    public static final RenderPipeline WORLD_TRIANGLES;
    public static final RenderPipeline WORLD_LINES;
    public static final RenderPipeline WORLD_TRIANGLES_DEPTH;
    public static final RenderPipeline WORLD_LINES_DEPTH;
    public static final RenderPipeline HUD_COLOR;
    public static final RenderPipeline HUD_ROUNDED;
    public static final RenderPipeline HUD_TEXT_SDF;
    public static final RenderPipeline HUD_TEX;

    private static RenderPipeline add(RenderPipeline pipeline) {
        PIPELINES.add(pipeline);
        return pipeline;
    }

    public static void precompile() {
        if (!isPrecompiled) break label_1;
        return;
        try {
            label_1:
            GpuDevice device = RenderSystem.getDevice();
            class_3300 resources = class_310.method_1551().method_1478();
            Object var2 = PIPELINES.iterator();
            while (var2.hasNext()) {
                RenderPipeline pipeline = ((RenderPipeline) var2.next());
                VieCheatsMod.LOGGER.info("[VieCheat] Precompiling pipeline: {}", pipeline.getLocation());
                device.precompilePipeline(pipeline, (identifier, shaderType) -> loadShaderSource(resources, identifier));
            }
            isPrecompiled = true;
            VieCheatsMod.LOGGER.info("[VieCheat] Successfully precompiled {} render pipelines", PIPELINES.size());
        } catch (Exception ex11) {
            Exception e = ex11;
            isPrecompiled = false;
            VieCheatsMod.LOGGER.error("[VieCheat] Failed to precompile pipelines", e);
        }
    }

    private static String loadShaderSource(class_3300 resources, class_2960 identifier) {
        Optional resource = resources.method_14486(identifier);
        if (!(resource.isPresent())) break label_13;
        try {
            InputStream in = ((class_3298) resource.get()).method_14482();
            try {
                Object var4 = IOUtils.toString(in, StandardCharsets.UTF_8);
                if (in != null) {
                    in.close();
                }
                return var4;
                try {
                } catch (Throwable ex6) {
                    var4 = ex6;
                    if (in == null) break label_10;
                    try {
                        in.close();
                    } catch (Throwable ex9) {
                        Object var5 = ex9;
                        var4.addSuppressed(var5);
                    }
                    label_10:
                    throw var4;
                }
            } catch (IOException ex11) {
                IOException e = ex11;
                throw new RuntimeException(("Failed to read shader: " + String.valueOf(identifier)), e);
            }
            label_13:
            String fallback = fallbackShaderSource(identifier);
            if (fallback != null) {
                VieCheatsMod.LOGGER.warn("[VieCheat] Shader not found: {}. Using built-in fallback.", identifier);
                return fallback;
            }
            VieCheatsMod.LOGGER.error("[VieCheat] Shader not found and no fallback: {}", identifier);
            return "";
        }
    }

    private static String fallbackShaderSource(class_2960 identifier) {
        String path = identifier.method_12832();
        Object var2 = path;
        int var3 = -1;
        switch (var2.hashCode()) {
            case 801318761 -> { break label_3; }
            case 801783464 -> { break label_2; }
            case 983554274 -> { break label_9; }
            case 984018977 -> { break label_8; }
            case 1005043100 -> { break label_7; }
            case 1005507803 -> { break label_6; }
            case 1149128934 -> { break label_5; }
            case 1149593637 -> { break label_4; }
            case 1861983988 -> { break label_11; }
            case 1862448691 -> { break label_10; }
            default -> { break label_12; }
        }
        label_2:
        if (!(var2.equals("shaders/pos_color.vert"))) break label_12;
        var3 = 0;
        break label_12;
        label_3:
        if (!(var2.equals("shaders/pos_color.frag"))) break label_12;
        var3 = 1;
        break label_12;
        label_4:
        if (!(var2.equals("shaders/hud_color.vert"))) break label_12;
        var3 = 2;
        break label_12;
        label_5:
        if (!(var2.equals("shaders/hud_color.frag"))) break label_12;
        var3 = 3;
        break label_12;
        label_6:
        if (!(var2.equals("shaders/hud_rounded.vert"))) break label_12;
        var3 = 4;
        break label_12;
        label_7:
        if (!(var2.equals("shaders/hud_rounded.frag"))) break label_12;
        var3 = 5;
        break label_12;
        label_8:
        if (!(var2.equals("shaders/hud_tex.vert"))) break label_12;
        var3 = 6;
        break label_12;
        label_9:
        if (!(var2.equals("shaders/hud_tex.frag"))) break label_12;
        var3 = 7;
        break label_12;
        label_10:
        if (!(var2.equals("shaders/hud_text_sdf.vert"))) break label_12;
        var3 = 8;
        break label_12;
        label_11:
        if (!(var2.equals("shaders/hud_text_sdf.frag"))) break label_12;
        var3 = 9;
        label_12:
        switch (var3) {
            case 0 -> { break label_13; }
            case 1 -> { break label_14; }
            case 2 -> { break label_15; }
            case 3 -> { break label_16; }
            case 4 -> { break label_17; }
            case 5 -> { break label_18; }
            case 6 -> { break label_19; }
            case 7 -> { break label_20; }
            case 8 -> { break label_21; }
            case 9 -> { break label_22; }
            default -> { break label_23; }
        }
        label_13:
        break label_24;
        label_14:
        break label_24;
        label_15:
        break label_24;
        label_16:
        break label_24;
        label_17:
        break label_24;
        label_18:
        break label_24;
        label_19:
        break label_24;
        label_20:
        break label_24;
        label_21:
        break label_24;
        label_22:
        break label_24;
        label_23:
        label_24:
        return null;
    }

    static {
        PIPELINES = new ArrayList();
        isPrecompiled = false;
        MESH_UNIFORMS = RenderPipeline.builder(new RenderPipeline.Snippet[0]).withUniform("MeshData", class_10789.field_60031).buildSnippet();
        var tmp0 = new RenderPipeline.Snippet[1];
        tmp0[0] = MESH_UNIFORMS;
        WORLD_TRIANGLES = add(RenderPipeline.builder(tmp0).withLocation(class_2960.method_60655("vie_cheats", "pipeline/world_triangles")).withVertexFormat(class_290.field_1576, VertexFormat.class_5596.field_27379).withVertexShader(class_2960.method_60655("vie_cheats", "shaders/pos_color.vert")).withFragmentShader(class_2960.method_60655("vie_cheats", "shaders/pos_color.frag")).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
        var tmp1 = new RenderPipeline.Snippet[1];
        tmp1[0] = MESH_UNIFORMS;
        WORLD_LINES = add(RenderPipeline.builder(tmp1).withLocation(class_2960.method_60655("vie_cheats", "pipeline/world_lines")).withVertexFormat(class_290.field_1576, VertexFormat.class_5596.field_29344).withVertexShader(class_2960.method_60655("vie_cheats", "shaders/pos_color.vert")).withFragmentShader(class_2960.method_60655("vie_cheats", "shaders/pos_color.frag")).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
        var tmp2 = new RenderPipeline.Snippet[1];
        tmp2[0] = MESH_UNIFORMS;
        WORLD_TRIANGLES_DEPTH = add(RenderPipeline.builder(tmp2).withLocation(class_2960.method_60655("vie_cheats", "pipeline/world_triangles_depth")).withVertexFormat(class_290.field_1576, VertexFormat.class_5596.field_27379).withVertexShader(class_2960.method_60655("vie_cheats", "shaders/pos_color.vert")).withFragmentShader(class_2960.method_60655("vie_cheats", "shaders/pos_color.frag")).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
        var tmp3 = new RenderPipeline.Snippet[1];
        tmp3[0] = MESH_UNIFORMS;
        WORLD_LINES_DEPTH = add(RenderPipeline.builder(tmp3).withLocation(class_2960.method_60655("vie_cheats", "pipeline/world_lines_depth")).withVertexFormat(class_290.field_1576, VertexFormat.class_5596.field_29344).withVertexShader(class_2960.method_60655("vie_cheats", "shaders/pos_color.vert")).withFragmentShader(class_2960.method_60655("vie_cheats", "shaders/pos_color.frag")).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
        HUD_COLOR = add(RenderPipeline.builder(new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655("vie_cheats", "pipeline/hud_color")).withVertexFormat(class_290.field_1576, VertexFormat.class_5596.field_27379).withVertexShader(class_2960.method_60655("vie_cheats", "shaders/hud_color.vert")).withFragmentShader(class_2960.method_60655("vie_cheats", "shaders/hud_color.frag")).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
        HUD_ROUNDED = add(RenderPipeline.builder(new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655("vie_cheats", "pipeline/hud_rounded")).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27379).withVertexShader(class_2960.method_60655("vie_cheats", "shaders/hud_rounded.vert")).withFragmentShader(class_2960.method_60655("vie_cheats", "shaders/hud_rounded.frag")).withUniform("RoundData", class_10789.field_60031).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
        HUD_TEXT_SDF = add(RenderPipeline.builder(new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655("vie_cheats", "pipeline/hud_text_sdf")).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27379).withVertexShader(class_2960.method_60655("vie_cheats", "shaders/hud_text_sdf.vert")).withFragmentShader(class_2960.method_60655("vie_cheats", "shaders/hud_text_sdf.frag")).withSampler("Sampler0").withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
        HUD_TEX = add(RenderPipeline.builder(new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655("vie_cheats", "pipeline/hud_tex")).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27379).withVertexShader(class_2960.method_60655("vie_cheats", "shaders/hud_tex.vert")).withFragmentShader(class_2960.method_60655("vie_cheats", "shaders/hud_tex.frag")).withSampler("Sampler0").withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.TRANSLUCENT).withCull(false).build());
    }

}
