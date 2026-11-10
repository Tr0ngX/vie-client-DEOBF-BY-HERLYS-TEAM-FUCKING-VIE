#version 330 core

uniform sampler2D Sampler0;

in vec2 v_Uv;
in vec4 v_Color;
out vec4 color;

void main() {
    float sdf = texture(Sampler0, v_Uv).a;
    float edge = 192.0 / 255.0;
    // Tighter AA window for crisper HUD/ClickGUI glyph edges.
    float aa = clamp(fwidth(sdf) * 0.48 + 0.0008, 0.0008, 0.028);
    float alpha = smoothstep(edge - aa, edge + aa, sdf);
    alpha = pow(alpha, 0.98);

    float outAlpha = v_Color.a * alpha;
    if (outAlpha <= 0.001) discard;
    color = vec4(v_Color.rgb, outAlpha);
}
