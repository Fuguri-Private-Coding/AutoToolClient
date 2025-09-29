#version 120

uniform sampler2D image;
uniform float alpha;

void main() {

    if (alpha <= 0.0) {
        discard;
    }

    vec2 uv = gl_TexCoord[0].st;
    vec4 color = texture2D(image, uv);

    if (alpha == 1.0) {
        gl_FragColor = color;
        return;
    }

    gl_FragColor = vec4(color.rgb, color.a * alpha);
}