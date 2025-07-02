#version 120

uniform sampler2D image;
uniform sampler2D image2;
uniform vec2 texel_size;
uniform vec2 direction;
uniform float radius;
uniform float kernel[128];

void main(void)
{
    vec2 uv = gl_TexCoord[0].st;

    if (direction.x == 0.0) {
        float alpha = texture2D(image2, uv).a;
        if (alpha > 0.0) discard;
    }

    vec4 pixel_color = texture2D(image, uv);
    pixel_color.rgb *= pixel_color.a;
    pixel_color *= kernel[0];

    for (float f = 0; f <= radius; f++) {
        vec2 offset = f * texel_size * direction;
        vec4 left = texture2D(image, uv - offset);
        vec4 right = texture2D(image, uv + offset);

        left.rgb *= left.a;
        right.rgb *= right.a;
        pixel_color += (left + right) * kernel[int(f)];
    }

    gl_FragColor = vec4(pixel_color.rgb / pixel_color.a, pixel_color.a);
}
