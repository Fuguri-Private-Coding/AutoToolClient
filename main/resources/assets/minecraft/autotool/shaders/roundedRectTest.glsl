#version 120

uniform float Factor;
uniform vec4 Color;

void main() {
    vec2 uv = gl_TexCoord[0].st;
    vec2 n = uv * 2 - 1;

    float d = pow(abs(n.x), Factor) + pow(abs(n.y), Factor);
    float derivative = fwidth(d);

    float alpha = 1.0 - smoothstep(1.0 - derivative, 1.0 + derivative, d);

    gl_FragColor = vec4(Color.rgb, Color.a * alpha);
}