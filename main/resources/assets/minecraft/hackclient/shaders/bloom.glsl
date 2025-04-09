#version 120
uniform sampler2D image;
uniform sampler2D image2;
uniform vec2 texel_size;
uniform vec2 direction;
uniform float radius;
uniform float kernel[128];
uniform vec4 color;
uniform float brightness;

void main() {
    vec2 uv = gl_TexCoord[0].st;

    if (direction.x == 0.0) {
        float alpha = texture2D(image2, uv).a;
        if (alpha > 0.0) discard;
    }

    vec4 pixelColor = texture2D(image, uv);
    pixelColor.rgb *= pixelColor.a;
    pixelColor *= kernel[0];

    for (float f = 1; f <= radius; f++) {
        vec2 offset = f * texel_size * direction;
        vec4 left = texture2D(image, uv - offset);
        vec4 right = texture2D(image, uv + offset);

        left.rgb *= left.a;
        right.rgb *= right.a;
        pixelColor += (left + right) * kernel[int(f)];
    }

    gl_FragColor = vec4(color.rgb, color.a * pixelColor.a * brightness);
}