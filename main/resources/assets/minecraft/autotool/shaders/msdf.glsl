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

    vec3 samples = texture2D(Sampler0, uv).rgb;
    float sigDist = median(samples) - 0.5;

    vec2 duv = fwidth(uv);
    float pixelDist = Range * length(duv * textureSize(Sampler0, 0));

    sigDist += Thickness;

    float fillAlpha = smoothstep(-Smoothness, Smoothness, sigDist / pixelDist);

    vec4 color = vec4(Color.rgb, Color.a * fillAlpha);

    gl_FragColor = color;
}