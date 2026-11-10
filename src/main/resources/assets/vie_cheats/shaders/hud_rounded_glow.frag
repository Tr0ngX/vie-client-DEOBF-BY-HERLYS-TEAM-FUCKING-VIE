#version 150

layout(std140) uniform RoundGlowData {
    // x = original width, y = original height
    // z = padPx (glowRadius + strokeThickness + 2)
    // w = min analytic AA width in px
    vec4 u_Rect;
    // Corner radii in px: tl, tr, br, bl
    vec4 u_Radii;
    // x = strokeThicknessPx, y = strokeAlign (-1 inner, 0 center, 1 outer), z/w unused
    vec4 u_Stroke;
    // x = glowRadiusPx, y = glowExponent, z/w unused
    vec4 u_Glow;
    // Layer colors in straight alpha (non-premultiplied)
    vec4 u_FillColor;
    vec4 u_StrokeColor;
    vec4 u_GlowColor;
};

in vec2 v_Uv;  // local pixel-space UV in expanded quad: [0..expandedW, 0..expandedH]
out vec4 fragColor;

float roundedRectSDF(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x  = (p.y > 0.0) ? r.x  : r.y;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

vec4 alphaOver(vec4 fg, vec4 bg) {
    float a = fg.a + bg.a * (1.0 - fg.a);
    vec3 rgb = fg.rgb * fg.a + bg.rgb * (1.0 - fg.a);
    return vec4(rgb, a);
}

void main() {
    vec2 size = max(u_Rect.xy, vec2(1e-5));
    float pad = max(0.0, u_Rect.z);
    float minAa = max(1e-4, u_Rect.w);

    vec2 halfSize = size * 0.5;
    vec2 local = v_Uv - vec2(pad + halfSize.x, pad + halfSize.y);
    // Flip Y so radius selection follows TL/TR/BR/BL semantic.
    vec2 p = vec2(local.x, -local.y);

    // SDF function expects radii by quadrant order:
    // (+x,+y), (+x,-y), (-x,+y), (-x,-y) = [tr, br, tl, bl]
    vec4 rr = vec4(u_Radii.y, u_Radii.z, u_Radii.x, u_Radii.w);
    float d = roundedRectSDF(p, halfSize, rr);
    float aa = max(fwidth(d), minAa);

    // Fill layer (inside shape) with derivative-based AA.
    float insideMask = smoothstep(aa, -aa, d);

    // Stroke layer: ring around SDF contour with optional alignment.
    float strokeThickness = max(0.0, u_Stroke.x);
    float strokeAlign = clamp(u_Stroke.y, -1.0, 1.0);
    float centerOffset = strokeAlign * (strokeThickness * 0.5);
    float localD = d - centerOffset;
    float strokeHalf = strokeThickness * 0.5;
    float strokeInnerEdge = localD - strokeHalf;
    float strokeOuterEdge = localD + strokeHalf;
    float strokeMask = smoothstep(-aa, aa, strokeOuterEdge) * smoothstep(aa, -aa, strokeInnerEdge);
    strokeMask *= step(1e-4, strokeThickness);

    // Glow layer: exponential decay outside the OUTER stroke boundary.
    float glowRadius = max(1e-4, u_Glow.x);
    float glowExponent = max(1e-4, u_Glow.y);
    float strokeOuterDistance = centerOffset + strokeHalf;
    float glowDist = max(0.0, d - strokeOuterDistance);
    float glowIntensity = exp(-glowDist * glowExponent / glowRadius);
    float glowOutsideMask = smoothstep(0.0, aa, glowDist);
    float glowMask = glowIntensity * glowOutsideMask;

    vec4 glowLayer = vec4(u_GlowColor.rgb, u_GlowColor.a * glowMask);
    vec4 strokeLayer = vec4(u_StrokeColor.rgb, u_StrokeColor.a * strokeMask);
    vec4 fillLayer = vec4(u_FillColor.rgb, u_FillColor.a * insideMask);

    // Back-to-front: Glow -> Stroke -> Fill.
    vec4 outColor = vec4(0.0);
    outColor = alphaOver(glowLayer, outColor);
    outColor = alphaOver(strokeLayer, outColor);
    outColor = alphaOver(fillLayer, outColor);

    if (outColor.a <= (1.0 / 255.0)) discard;
    fragColor = outColor;
}
