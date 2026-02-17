#version 130

uniform sampler2D Sampler0;
uniform float Range;
uniform float Thickness;
uniform float Smoothness;
uniform vec4 Color;
uniform bool Outline;
uniform float OutlineThickness;
uniform vec4 OutlineColor;

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

    vec4 color;
    if (Outline) {
        float outlineDist = sigDist + OutlineThickness;
        float outlineAlpha = smoothstep(-Smoothness, Smoothness, outlineDist / pixelDist);

        color = mix(OutlineColor, vec4(1.0, 1.0, 1.0, 1.0), fillAlpha);
        color.a = outlineAlpha;
    } else {
        color = vec4(Color.rgb, Color.a * fillAlpha);
    }

//    if (color.a == 0.0) discard;

    gl_FragColor = color;
}