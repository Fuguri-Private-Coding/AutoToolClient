#version 120

uniform vec4 texColor;

uniform sampler2D MSDFTex;
uniform float pxRange;

float median(float a, float b, float c) {
    return max(min(a, b), min(max(a, b), c));
}

void main() {
    vec2 uv = gl_TexCoord[0].st;

    vec3 msdf = texture2D(MSDFTex, uv).rgb;
    float sd = median(msdf.r, msdf.g, msdf.b);

    float afwidth = fwidth(sd);
    afwidth = max(afwidth, 1e-6);

    float smoothing = pxRange * afwidth;
    float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, sd);

    vec4 base = texColor;
    base.a *= alpha;

    if (base.a == 0.0) {
        discard;
    }

    gl_FragColor = base;
}