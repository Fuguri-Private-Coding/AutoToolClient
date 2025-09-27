#version 120

uniform sampler2D Image;
uniform sampler2D Mask;

uniform int Test;

void main() {
    vec2 uv = gl_TexCoord[0].st;

    float mask_alpha = texture2D(Mask, uv).a;

    if (mask_alpha == 0.0) {
        discard;
    }

    vec4 image_color = texture2D(Image, uv);
    float final_alpha = image_color.a * mask_alpha;

    if (Test > 0) {
        image_color.rgb *= final_alpha;
    }

    gl_FragColor = vec4(image_color.rgb, final_alpha);
}
