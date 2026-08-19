#version 120

uniform sampler2D Sampler0;
uniform vec2 Size;
uniform vec4 Radius;
uniform float Smoothness;
uniform vec4 Color;

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

void main() {
    vec2 uv = gl_TexCoord[0].st;

    float alpha = ralpha(Size, uv, Radius, Smoothness);
    vec4 color = vec4(Color.rgb, alpha * Color.a) * texture2D(Sampler0, uv);

    if (color.a == 0.0) {
        discard;
    }

    gl_FragColor = color;
}