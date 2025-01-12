#version 120

uniform sampler2D texture;
uniform float texel_size;

void main( void ) {
    vec2 zv = gl_TexCoord[0].st;
    vec4 pix_color = texture2D(texture, zv);

    if (pix_color.a > 0.0) {
        if (zv.y > 0.0 && zv.y < 0.33) {
            gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
        }

        if (zv.y > 0.33 && zv.y < 0.66) {
            gl_FragColor = vec4(0.0, 0.0, 1.0, 1.0);
        }

        if (zv.y > 0.66 && zv.y < 1.0) {сб
            gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
        }
    } else {
        discard;
    }
}