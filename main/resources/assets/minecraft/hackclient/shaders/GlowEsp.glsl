#version 120

uniform float radius;
uniform sampler2D entityTexture;
uniform vec4 color;
uniform float direction;
uniform float u_texel_size;
uniform

void main( void ) {

    vec2 uv = gl_TexCoord[0].st;
    vec4 pix_color = texture2D(entityTexture, uv)

    if (pix_color.a == 0.0) {
        for(int i = -radius; i <= radius; i++) {
            vec2 pos = uv;

            if (direction == 0.0) {
                pos = vec2(uv.x + i, uv.y);
            } else {
                pos = vec2(uv.x, uv.y + ui)
            }

            vec4 temp_color = texture2D(entityTexture, uv + i);
            color += temp_color * 0.01;
        }
    }

     gl_FragColor = color;
}