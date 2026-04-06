#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

uniform vec3 Phosphor = vec3(0.7, 0.0, 0.0);

void main() {
    vec2 uv = gl_TexCoord[0].st;

    vec4 CurrTexel = texture2D(DiffuseSampler, uv);
    vec4 PrevTexel = texture2D(PrevSampler, uv);
    float factor = Phosphor.r;

    if (Phosphor.g == 1) {
        gl_FragColor = vec4(max(PrevTexel.rgb * vec3(factor), CurrTexel.rgb), 1.0);
    } else {
        gl_FragColor = vec4(mix(PrevTexel.rgb, CurrTexel.rgb, factor), 1.0);
    }
}