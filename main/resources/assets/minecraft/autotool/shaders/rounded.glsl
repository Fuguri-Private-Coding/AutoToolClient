#version 120

uniform vec2 u_size; // rectangle size
uniform vec4 u_radius; // radius for each vertex
uniform vec4 u_color; // radius for each vertex
uniform float u_smooth;

float rdist(vec2 pos, vec2 size, vec4 radius) {
    radius.xy = (pos.x > 0.0) ? radius.xy : radius.wz;
    radius.x  = (pos.y > 0.0) ? radius.x : radius.y;

    vec2 v = abs(pos) - size + radius.x;
    return min(max(v.x, v.y), 0.0) + length(max(v, 0.0)) - radius.x;
}

float ralpha(vec2 size, vec2 coord, vec4 radius) {
    vec2 center = size * 0.5;
    float dist = rdist(center - (coord * size), center - 1.0, radius);
    return 1.0 - smoothstep(1.0 - u_smooth, 1.0, dist);
}

void main() {
    vec2 uv = gl_TexCoord[0].st;

    float alpha = ralpha(u_size, uv, u_radius);
    vec4 color = vec4(u_color.rgb, u_color.a * alpha);

    if (color.a == 0.0) { // alpha test
        discard;
    }

    gl_FragColor = color;
}