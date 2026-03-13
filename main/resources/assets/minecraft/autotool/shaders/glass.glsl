#version 120

uniform sampler2D Sampler0;
uniform vec2 ScreenSize;
uniform vec2 Size;

uniform vec4 Radius;
uniform float Smoothness;
uniform float CornerSmoothness;
uniform float GlobalAlpha;

uniform float FresnelPower;
uniform vec3 FresnelColor;
uniform float FresnelAlpha;
uniform float BaseAlpha;
uniform bool FresnelInvert;
uniform float FresnelMix;
uniform float DistortStrength;
uniform float FlipY;

float roundedBoxSDF(vec2 p, vec2 b, vec4 r, float smoothness) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x = (p.y > 0.0) ? r.x : r.y;

    vec2 q = abs(p) - b + r.x;
    vec2 qClamped = max(q, 0.0);

    float len = pow(
        pow(qClamped.x, smoothness) + pow(qClamped.y, smoothness),
        1.0 / smoothness
    );

    return min(max(q.x, q.y), 0.0) + len - r.x;
}

vec2 safeNormalize(vec2 v) {
    float len = length(v);
    return len > 0.0001 ? v / len : vec2(0.0, 0.0);
}

void main() {
    vec2 uv = gl_TexCoord[0].st;

/*
     * Локальные координаты прямоугольника в framebuffer pixels.
     */
    vec2 center = Size * 0.5;
    vec2 halfSize = max(center - vec2(1.0), vec2(0.0));
    vec2 pos = uv * Size - center;

    float cornerPower = max(CornerSmoothness, 0.001);
    float sdf = roundedBoxSDF(-pos, halfSize, Radius, cornerPower);

/*
     * Маска rounded-rect.
     */
    float edgeSoftness = max(Smoothness, 0.0001);
    float alphaMask = 1.0 - smoothstep(0.0, edgeSoftness, sdf);

/*
     * Градиент к краям для стеклянного блика.
     */
    float maxDist = max(min(halfSize.x, halfSize.y), 0.0001);
    float edgeGradient = 1.0 - clamp(abs(sdf) / maxDist, 0.0, 1.0);

    float base = FresnelInvert ? edgeGradient : (1.0 - edgeGradient);

    float fresnel;
    if (FresnelPower > 20.0) {
        fresnel = exp(FresnelPower * log(clamp(base, 0.001, 1.0)));
    } else {
        fresnel = pow(clamp(base, 0.0, 1.0), FresnelPower);
    }
    fresnel = clamp(fresnel, 0.0, 1.0);

/*
     * Ключевой фикс:
     * читаем Sampler0 через экранную позицию текущего пикселя,
     * поэтому фон внутри стекла всегда совпадает с тем, что под ним.
     */
    vec2 screenUV = gl_FragCoord.xy / ScreenSize;

    if (FlipY > 0.5) {
        screenUV.y = 1.0 - screenUV.y;
    }

    vec2 dir = safeNormalize(pos);

/*
     * Небольшая дисторсия от центра к краям.
     * DistortStrength задается в пикселях.
     */
    vec2 distortion = dir * fresnel * (DistortStrength / ScreenSize);
    vec2 sampleUV = clamp(screenUV + distortion, vec2(0.0), vec2(1.0));

    vec4 texColor = texture2D(Sampler0, sampleUV);

    vec3 finalColor = mix(texColor.rgb, FresnelColor, fresnel * FresnelMix);
    float finalAlpha = mix(BaseAlpha, FresnelAlpha, fresnel) * alphaMask * GlobalAlpha;

    if (finalAlpha < 0.001) {
        discard;
    }

    gl_FragColor = vec4(finalColor, finalAlpha);
}