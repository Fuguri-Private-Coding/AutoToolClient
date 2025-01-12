#version 120

uniform sampler2D texture;
uniform vec4 startColor;
uniform vec4 endColor;
uniform float texel_size;

void main( void ) {
    vec2 zv = gl_TexCoord[0].st;
    vec4 pix_color = texture2D(texture, zv);

    if (pix_color.a == 0.0) {
        discard;
    }

    gl_FragColor = mix(startColor, endColor, zv.y);
}