#version 120

uniform float time;
uniform sampler2D texture;
uniform float texel_size;
uniform float r_offset;
uniform float g_offset;
uniform float b_offset;

void main( void ) {
    vec2 zv = gl_TexCoord[0].st;
    vec4 pix_color = texture2D(texture, zv);

    if (pix_color.a > 0.0) {
        vec2 p = zv.xy;
        for(int i = 1; i < 10; i++) {
            p += sin(p.yx * vec2(1.6, 1.1) * float(i + 11) + time * float(i) * vec2(3.4, 0.5) / 10.0) * 0.1;
        }
        float c = (abs(sin(p.y + time * 0.0) + sin(p.x + time * 0.0))) * 0.5;
        gl_FragColor = vec4(vec3(r_offset + c, g_offset + c, b_offset + c), pix_color.a);
    } else {
        discard;
    }
}