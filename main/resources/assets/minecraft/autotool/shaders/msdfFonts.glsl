#version 130

uniform sampler2D Sampler0;
uniform float Range;
uniform float Thickness;
uniform float Smoothness;
uniform vec4 Color;

float median(vec3 color) {
    return max(min(color.r, color.g), min(max(color.r, color.g), color.b));
}

void main() {
    vec2 uv = gl_TexCoord[0].st;

    float dist = median(texture2D(Sampler0, uv).rgb) - 0.5 + Thickness;
    vec2 h = vec2(dFdx(uv.x), dFdy(uv.y)) * textureSize(Sampler0, 0);
    float pixels = Range * inversesqrt(h.x * h.x + h.y * h.y);
    float alpha = smoothstep(-Smoothness, Smoothness, dist * pixels);

    gl_FragColor = vec4(Color.rgb, Color.a * alpha);
}