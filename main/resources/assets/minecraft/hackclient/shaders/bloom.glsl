#version 120

uniform vec4 main_color;
uniform sampler2D texture;
uniform float radius;
uniform vec2 texel_size;
uniform vec2 direction;

void main ( void ) {

    vec2 uv = gl_TexCoord[0].st;
    vec4 pix_color = texture2D(texture, uv);

    if (pix_color.a > 0.0) {
        discard;
    }

    vec4 final_color = vec4(0.0, 0.0, 0.0, 1.0);

    for (float f = 0; f <= radius; f++) {
        vec2 offset = f * texel_size * direction;
        vec4 color_in_offset_left = texture2D(texture, uv - offset);
        vec4 color_in_offset_right = texture2D(texture, uv + offset);

        if (color_in_offset_left.a == 0.0 && color_in_offset_right.a == 0.0) {
            continue;
        }

        color_in_offset_left.rgb *= color_in_offset_left.a;
        color_in_offset_right.rgb *= color_in_offset_right.a;
        final_color.rgb += (color_in_offset_left.rgb + color_in_offset_right.rgb) / 2 / abs(int(f));
    }

    gl_FragColor = vec4(final_color.rgb, 1.0);
}
