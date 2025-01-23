#version 120

uniform vec4 main_color;
uniform Sampler2D texture;
uniform float radius;
uniform vec2 texel_size;

void main ( void ) {

    vec2 uv = gl_TexCoord[0].st;
    vec4 pix_color = texture2D(texture, uv);

    if (pix_color.a > 0.0) {
        discard;
    }

    vec4 final_color = vec4(vec3(0.0), 1.0);

    for (float f = -radius; f <= radius; f++) {
        for (float f1 = -radius; f1 <= radius; f1++) {
            vec2 offset = vec2(f, f1) * texel_size;
            vec4 color_in_offset = texture2D(texture, offset);

            color_in_offset.rgb *= color_in_offset.a;

            if (color_in_offset.a == 0.0) {
                continue;
            }

            final_color.rgb += color_in_offset.rgb / length(offset);
        }
    }

    gl_FragColor = vec4(final_color.rgb, 1.0);
}
