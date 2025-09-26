#version 120

uniform sampler2D u_diffuse_sampler;
uniform sampler2D u_other_sampler;
uniform vec2 u_texel_size;
uniform vec2 u_direction;
uniform float u_radius;
uniform float u_kernel[128];

uniform vec2 iResolution;
uniform vec2 iMouse;
uniform sampler2D iChannel0;

// SDF of a rounded rectangle
float sdfRect(vec2 center, vec2 size, vec2 p, float r)
{
    vec2 p_rel = p - center;
    vec2 q = abs(p_rel) - size;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

// Thickness is the t in the doc.
vec3 getNormal(float sd, float thickness)
{
    float dx = dFdx(sd);
    float dy = dFdy(sd);

    // The cosine and sine between normal and the xy plane.
    float n_cos = max(thickness + sd, 0.0) / thickness;
    float n_sin = sqrt(1.0 - n_cos * n_cos);

    return normalize(vec3(dx * n_cos, dy * n_cos, n_sin));
}

// The height (z component) of the pad surface at sd.
float height(float sd, float thickness)
{
    if(sd >= 0.0) return 0.0;
    if(sd < -thickness) return thickness;

    float x = thickness + sd;
    return sqrt(thickness * thickness - x * x);
}

vec4 bgImage(vec2 uv)
{
    return texture2D(iChannel0, uv);
}

vec4 bgStrips(vec2 uv)
{
    if(fract(uv.y * iResolution.y / 20.0) < 0.5)
    {
        return vec4(0.0, 0.5, 1.0, 0.0);
    }
    else
    {
        return vec4(0.9, 0.9, 0.9, 0.0);
    }
}

vec4 bg(vec2 uv)
{
    if(uv.x > 0.5) return bgStrips(uv);
    return bgImage(uv);
}

void main()
{
    vec2 fragCoord = gl_TexCoord[0].st * iResolution;
    vec2 uv = gl_TexCoord[0].st;

    // Проверка альфа-канала как во втором шейдере
    float alpha = texture2D(u_other_sampler, uv).a;
    if (u_direction.x == 0.0 && alpha == 0.0) {
        discard;
    }

    // Применяем blur kernel как во втором шейдере
    float half_radius = u_radius / 2.0;
    vec4 pixel_color = texture2D(u_diffuse_sampler, uv) * u_kernel[0];

    for (float f = 1.0; f <= u_radius; f++) {
        vec2 offset = f * u_texel_size * u_direction;
        pixel_color += texture2D(u_diffuse_sampler, uv - offset) * u_kernel[int(f)];
        pixel_color += texture2D(u_diffuse_sampler, uv + offset) * u_kernel[int(f)];
    }

    // Если это горизонтальный пасс (u_direction.x != 0), используем эффект рефракции
    if (u_direction.x != 0.0) {
        float thickness = 15.0;
        float index = 1.5;
        float base_height = thickness * 8.0;
        float color_mix = -0.2;
        vec4 color_base = vec4(1.0, 1.0, 1.0, 0.0);

        vec2 center = iMouse.xy;
        if (center == vec2(0.0, 0.0)) {
            center = iResolution.xy * 0.5;
        }

        float sd = sdfRect(center, vec2(128.0, 0.0), fragCoord, 64.0);

        // Background pass-through with anti-aliasing
        vec4 bg_col = mix(vec4(0.0), bg(uv), clamp(sd / 100.0, 0.0, 1.0) * 0.1 + 0.9);
        bg_col.a = smoothstep(-4.0, 0.0, sd);

        vec3 normal = getNormal(sd, thickness);

        // Рефракция
        vec3 incident = vec3(0.0, 0.0, -1.0);
        vec3 refract_vec = refract(incident, normal, 1.0 / index);
        float h = height(sd, thickness);
        float refract_length = (h + base_height) / dot(vec3(0.0, 0.0, -1.0), refract_vec);
        vec2 coord1 = fragCoord + refract_vec.xy * refract_length;
        vec4 refract_color = bg(coord1 / iResolution.xy);

        // Отражение
        vec3 reflect_vec = reflect(incident, normal);
        vec4 reflect_color = vec4(0.0);
        float c = clamp(abs(reflect_vec.x - reflect_vec.y), 0.0, 1.0);
        reflect_color = vec4(c, c, c, 0.0);

        // Смешиваем с blur результатом
        vec4 effect_color = mix(mix(refract_color, reflect_color, (1.0 - normal.z) * 2.0),
                                color_base, color_mix);

        effect_color = clamp(effect_color, 0.0, 1.0);
        bg_col = clamp(bg_col, 0.0, 1.0);

        pixel_color = mix(effect_color, bg_col, bg_col.a);
    }

    gl_FragColor = vec4(pixel_color.rgb, u_direction.x == 0.0 ? alpha : 1.0);
}