#ifdef GL_ES
precision mediump float;
#endif

uniform float time;
uniform vec2 resolution;

float rand(float n) {
    return fract(sin(n) * 43758.5453);
}

float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

float circle(vec2 uv, vec2 center, float radius, float softness) {
    float dist = length(uv - center);
    return smoothstep(radius + softness, radius - softness, dist);
}

vec3 getRandomPink(float r) {
    return mix(vec3(1.0, 0.0, 0.5), vec3(1.0, 0.4, 0.7), r);
}

vec3 getRandomBlue(float r) {
    return mix(vec3(0.1, 0.3, 1.0), vec3(0.4, 0.6, 1.0), r);
}

void main() {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    uv = uv * 2.0 - 1.0;
    uv.x *= resolution.x / resolution.y;

    vec3 baseColor = vec3(0.8); // серый фон, белый так се да
    vec3 color = baseColor;

    const int NUM_CIRCLES = 60;
    float t = time;

    for (int i = 0; i < NUM_CIRCLES; i++) {
        float fi = float(i);

        float seed = fi * 10.0;
        float speed = 0.05 + rand(seed + 0.1) * 0.1;

        float xPos = -1.2 + rand(seed + 1.0) * 2.4;
        float sway = sin(t * speed + fi) * 0.25 * rand(seed + 2.0);

        vec2 center = vec2(
            xPos + sway,
            -1.2 + mod(t * speed + rand(seed + 3.0) * 10.0, 2.4)
        );

        float sizeBase = rand(seed + 4.0);
        float size = mix(0.10, 0.30, pow(sizeBase, 2.0));

        float alpha = circle(uv, center, size, 0.08);

        float colorSeed = rand(seed + 5.0);
        vec3 bubbleColor = mod(fi, 2.0) < 1.0
            ? getRandomPink(colorSeed)
            : getRandomBlue(colorSeed);

        color = mix(color, bubbleColor, alpha * 0.6);
    }

    gl_FragColor = vec4(color, 1.0);
}