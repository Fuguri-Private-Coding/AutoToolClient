#version 120

uniform sampler2D image;
uniform float alpha;

void main() {
    vec2 uv = gl_TexCoord[0].st;
    vec4 color = texture2D(image, uv);
    gl_FragColor = vec4(color.rgb, color.a * (alpha));
}