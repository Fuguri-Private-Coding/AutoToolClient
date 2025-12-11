#version 120

uniform sampler2D bgImage;
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
//

///**
// * Вычисляет Signed Distance Function (SDF) для прямоугольника с возможностью
// * задания индивидуальных скруглений для каждого угла.
// *
// * @param CenterPosition - координата точки относительно центра прямоугольника.
// * @param Size - половина ширины и высоты прямоугольника (т.е. от центра до края).
// * @param Radius - радиусы скругления углов в следующем порядке:
// *                 (верхний левый, верхний правый, нижний правый, нижний левый).
// *
// * @return float - расстояние от точки до ближайшей поверхности прямоугольника.
// *                 Отрицательное значение — внутри, положительное — снаружи.
// */
//float roundedBoxSDF(vec2 CenterPosition, vec2 Size, vec4 Radius) {
//    // Ограничиваем радиусы, чтобы они не превышали половины сторон прямоугольника.
//    vec2 halfSize = Size;
//    Radius = min(Radius, vec4(halfSize.x, halfSize.y, halfSize.x, halfSize.y));
//
//    // Выбираем радиус в зависимости от квадранта, в котором находится точка.
//    Radius.xy = (CenterPosition.x > 0.0) ? Radius.xy : Radius.zw;
//    Radius.x  = (CenterPosition.y > 0.0) ? Radius.x  : Radius.y;
//
//    // Смещаем координаты на радиус и вычисляем расстояние.
//    vec2 q = abs(CenterPosition) - Size + Radius.x;
//    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - Radius.x;
//}
//
///**
// * Я ебал меня сосали
// *
// * Вершины идут по часовой стрелке:
// * 0 — левый верхний (0, 0)
// * 1 — левый нижний (0, 1)
// * 2 — правый нижний (1, 1)
// * 3 — правый верхний (1, 0)
// */
//const vec2[4] RECT_VERTICES_COORDS = vec2[] (
//vec2(0.0, 0.0),
//vec2(0.0, 1.0),
//vec2(1.0, 1.0),
//vec2(1.0, 0.0)
//);
//
//vec2 rvertexcoord(int id) {
//    return RECT_VERTICES_COORDS[id % 4];
//}

float rdist(vec2 pos, vec2 size, vec4 radius) {
    radius.xy = (pos.x > 0.0) ? radius.xy : radius.wz;
    radius.x  = (pos.y > 0.0) ? radius.x : radius.y;

    vec2 v = abs(pos) - size + radius.x;
    return min(max(v.x, v.y), 0.0) + length(max(v, 0.0)) - radius.x;
}

float ralpha(vec2 size, vec2 coord, vec4 radius, float smoothness) {
    vec2 center = size * 0.5;
    float dist = rdist(center - (coord * size), center - 1.0, radius);
    return 1.0 - smoothstep(1.0 - smoothness, 1.0, dist);
}


float roundedBoxSDF(vec2 p, vec2 b, vec4 r, float smoothness) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x = (p.y > 0.0) ? r.x : r.y;
    vec2 q = abs(p) - b + r.x;
    vec2 q_clamped = max(q, 0.0);
    float len = pow(pow(q_clamped.x, smoothness) + pow(q_clamped.y, smoothness), 1.0/smoothness);
    return min(max(q.x, q.y), 0.0) + len - r.x;
}

void main() {
    vec2 uv = gl_TexCoord[0].st;

    vec2 center = Size * 0.5;
    vec2 box_half_size = center - 1.0;
    vec2 pos = (uv * Size) - center;

    float distance = roundedBoxSDF(-pos, box_half_size, Radius, CornerSmoothness);
    float alpha = 1.0 - smoothstep(1.0 - Smoothness, 1.0, distance);

    float distToEdge = abs(roundedBoxSDF(pos, box_half_size, Radius, CornerSmoothness));

    float max_dist_norm = min(box_half_size.x, box_half_size.y);
    float edge_gradient = 1.0 - clamp(distToEdge / max_dist_norm, 0.0, 1.0);

    float fresnel;
    float base = FresnelInvert ? edge_gradient : (1.0 - edge_gradient);

    if (FresnelPower > 20.0) {
        fresnel = exp(FresnelPower * log(clamp(base, 0.001, 1.0)));
    } else {
        fresnel = pow(base, FresnelPower);
    }
    fresnel = clamp(fresnel, 0.0, 1.0);

    vec2 dir = normalize(pos);
    vec2 distortedTexCoord = uv + dir * fresnel * DistortStrength;

    vec4 texColor = texture2D(bgImage, distortedTexCoord) * FragColor;

    vec3 finalColor = mix(texColor.rgb, FresnelColor, fresnel * FresnelMix);
    float finalAlpha = mix(BaseAlpha, FresnelAlpha, fresnel) * alpha;

    if (finalAlpha < 0.001) {
        discard;
    }

    gl_FragColor = vec4(finalColor, finalAlpha * GlobalAlpha);
}
