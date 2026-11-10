#version 330 core

layout (std140) uniform RoundData {
    vec4 u_RoundA;
    vec4 u_RoundB;
    vec4 u_RoundC;
};

in vec2 v_Uv;
in vec4 v_Color;
out vec4 color;

float roundedRectSdfVarying(vec2 p, vec2 halfSize, vec4 radii) {
    // radii: tl, tr, br, bl
    float radius = radii.x;
    if (p.x > 0.0 && p.y < 0.0) {
        radius = radii.y;
    } else if (p.x > 0.0 && p.y >= 0.0) {
        radius = radii.z;
    } else if (p.x <= 0.0 && p.y >= 0.0) {
        radius = radii.w;
    }
    vec2 q = abs(p) - (halfSize - vec2(radius));
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

void main() {
    float width = u_RoundA.x;
    float height = u_RoundA.y;
    float softness = max(0.001, u_RoundA.z);
    float mode = u_RoundA.w; // 0 = fill, 1 = stroke

    float stroke = max(0.0, u_RoundB.x);
    vec4 radii = vec4(u_RoundB.y, u_RoundB.z, u_RoundB.w, u_RoundC.x);

    vec2 size = vec2(width, height);
    vec2 p = v_Uv - size * 0.5;
    vec2 halfSize = size * 0.5;

    float d = roundedRectSdfVarying(p, halfSize, radii);
    float fillAlpha = 1.0 - smoothstep(0.0, softness, d);
    float alpha = fillAlpha;

    // Stroke ring rendered inside bounds: d in [-stroke, 0].
    if (mode > 0.5) {
        float innerEdge = smoothstep(-stroke - softness, -stroke + softness, d);
        float outerEdge = 1.0 - smoothstep(0.0, softness, d);
        alpha = innerEdge * outerEdge;
    }

    float outAlpha = v_Color.a * alpha;
    if (outAlpha <= 0.001) discard;
    color = vec4(v_Color.rgb, outAlpha);
}
