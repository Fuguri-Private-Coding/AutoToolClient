#version 120

uniform sampler2D entityTexture;
uniform float radius;
uniform float u_texel_size;

void main( void ) {
    vec2 uv = gl_TexCoord[0].st;
    vec4 pix_color = texture2D(entityTexture, uv)

    if (pix_color.a == 0.0) {
        vec4 color = vec4(0.0, 0.0, 0.0, 1.0);
        for(float f = -radius; f <= radius; f++) {
            float offset = f * u_texel_size;

            vec4 left = texture2D(entityTexture, vec2(uv.x - offset, uv.y));
            vec4 right = texture2D(entityTexture, vec2(uv.x + offset, uv.y);

            color += (left + right) / abs(f);
        }
        gl_FragColor = color;
    }
}