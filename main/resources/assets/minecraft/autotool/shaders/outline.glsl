#version 120

uniform vec2 texelSize, direction;
uniform sampler2D texture;
uniform float radius;
uniform vec3 color;

void main() {
    vec2 uv = gl_TexCoord[0].st;

    float centerAlpha = texture2D(texture, uv).a;
    float innerAlpha = centerAlpha;

    vec2 offset = direction * texelSize;
    for (float r = 1.0; r <= radius; r++) {
        float alphaCurrent1 = texture2D(texture, uv + offset * r).a;
        float alphaCurrent2 = texture2D(texture, uv - offset * r).a;

        innerAlpha += alphaCurrent1 + alphaCurrent2;
    }

    gl_FragColor = vec4(color, innerAlpha) * step(0.0, -centerAlpha);
}